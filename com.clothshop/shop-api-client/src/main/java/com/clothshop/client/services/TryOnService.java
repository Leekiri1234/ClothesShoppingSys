package com.clothshop.client.services;

import com.clothshop.client.dtos.response.ProductDetailResponse;
import com.clothshop.client.dtos.response.VariantDetailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Virtual Try-On Service using OpenAI GPT Image 1.
 *
 * Flow:
 *   1. User uploads portrait photo
 *   2. User selects a product from the catalog
 *   3. Service downloads the product image from its DB URL
 *   4. Both images are sent to GPT Image 1 edits API
 *   5. AI generates result: user wearing the selected clothing
 *
 * Security:
 *   - Input validation (file type, size, dimensions)
 *   - Safe product image download (timeout, size limit, URL whitelist)
 *   - Prompt injection protection
 *   - Comprehensive exception handling
 */
@Service
@Slf4j
public class TryOnService {

    @Value("${openai.api.key:}")
    private String openAiApiKey;

    private static final String OPENAI_EDIT_URL = "https://api.openai.com/v1/images/edits";

    // Limits
    private static final long MAX_USER_IMAGE_BYTES = 10 * 1024 * 1024;   // 10MB
    private static final long MAX_PRODUCT_IMAGE_BYTES = 5 * 1024 * 1024; // 5MB
    private static final int TARGET_SIZE = 1024;
    private static final Duration DOWNLOAD_TIMEOUT = Duration.ofSeconds(15);
    private static final Duration API_TIMEOUT = Duration.ofSeconds(120);

    // Allowed image content types
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/png", "image/jpeg", "image/jpg", "image/webp"
    );

    private final ProductClientService productClientService;
    private final RestTemplate restTemplate;

    public TryOnService(ProductClientService productClientService) {
        this.productClientService = productClientService;
        this.restTemplate = new RestTemplate();
    }

    // ─────────────────────────────────────────────────────────────
    //  PUBLIC API
    // ─────────────────────────────────────────────────────────────

    /**
     * Generate a virtual try-on image.
     *
     * @param userImage  The user's portrait photo (uploaded file)
     * @param productId  The selected product's ID from the catalog
     * @return URL of the generated image from OpenAI
     */
    public String generateTryOnImage(MultipartFile userImage, Long productId) throws Exception {
        // ── Step 1: Validate API key ──
        if (openAiApiKey == null || openAiApiKey.isBlank()) {
            throw new IllegalStateException("OpenAI API key chưa được cấu hình. Vui lòng liên hệ quản trị viên.");
        }

        // ── Step 2: Validate user image ──
        validateUserImage(userImage);

        // ── Step 3: Fetch product details from database ──
        ProductDetailResponse product = fetchProduct(productId);

        // ── Step 4: Download product image from its URL ──
        byte[] productImageBytes = downloadProductImage(product);

        // ── Step 5: Process user image to 1024x1024 PNG ──
        byte[] userImageBytes = resizeToPng(userImage.getBytes(), "user");

        // ── Step 6: Process product image to 1024x1024 PNG ──
        byte[] productImagePng = resizeToPng(productImageBytes, "product");

        // ── Step 7: Build detailed prompt from product data ──
        String prompt = buildSecurePrompt(product);
        log.info("Virtual Try-On | Product: {} | Prompt length: {}", product.getProductName(), prompt.length());

        // ── Step 8: Call GPT Image 1 API ──
        return callGptImage1(userImageBytes, productImagePng, prompt);
    }

    // ─────────────────────────────────────────────────────────────
    //  VALIDATION
    // ─────────────────────────────────────────────────────────────

    private void validateUserImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng tải lên ảnh chân dung của bạn.");
        }
        if (file.getSize() > MAX_USER_IMAGE_BYTES) {
            throw new IllegalArgumentException(
                    String.format("Ảnh quá lớn (%dMB). Giới hạn tối đa là %dMB.",
                            file.getSize() / (1024 * 1024), MAX_USER_IMAGE_BYTES / (1024 * 1024)));
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Định dạng ảnh không hỗ trợ. Vui lòng sử dụng PNG, JPEG hoặc WebP.");
        }
        // Verify it's actually a valid image by attempting to decode
        try {
            BufferedImage img = ImageIO.read(file.getInputStream());
            if (img == null) {
                throw new IllegalArgumentException("File tải lên không phải là ảnh hợp lệ.");
            }
            if (img.getWidth() < 50 || img.getHeight() < 50) {
                throw new IllegalArgumentException("Ảnh quá nhỏ. Vui lòng sử dụng ảnh có độ phân giải tối thiểu 50x50.");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Không thể đọc file ảnh. Vui lòng thử lại với ảnh khác.");
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  PRODUCT DATA
    // ─────────────────────────────────────────────────────────────

    private ProductDetailResponse fetchProduct(Long productId) {
        if (productId == null || productId <= 0) {
            throw new IllegalArgumentException("ID sản phẩm không hợp lệ.");
        }
        try {
            ProductDetailResponse product = productClientService.getProductById(productId);
            if (product == null) {
                throw new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + productId);
            }
            return product;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching product {}: {}", productId, e.getMessage());
            throw new RuntimeException("Không thể tải thông tin sản phẩm. Vui lòng thử lại.");
        }
    }

    private byte[] downloadProductImage(ProductDetailResponse product) {
        // Try main imageUrl first, then fall back to first image in the images list
        String imageUrl = product.getImageUrl();
        if ((imageUrl == null || imageUrl.isBlank()) && product.getImages() != null && !product.getImages().isEmpty()) {
            imageUrl = product.getImages().get(0);
        }
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalStateException(
                    "Sản phẩm \"" + product.getProductName() + "\" chưa có ảnh. Không thể thực hiện thử đồ.");
        }

        log.debug("Downloading product image from: {}", imageUrl);

        try {
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(DOWNLOAD_TIMEOUT)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(imageUrl))
                    .timeout(DOWNLOAD_TIMEOUT)
                    .GET()
                    .build();

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                throw new IOException("HTTP " + response.statusCode());
            }

            byte[] body = response.body();
            if (body == null || body.length == 0) {
                throw new IOException("Empty response body");
            }
            if (body.length > MAX_PRODUCT_IMAGE_BYTES) {
                throw new IOException("Product image too large: " + body.length + " bytes");
            }

            // Verify it's actually an image
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(body));
            if (img == null) {
                throw new IOException("Downloaded file is not a valid image");
            }

            return body;
        } catch (Exception e) {
            log.error("Failed to download product image from {}: {}", imageUrl, e.getMessage());
            throw new RuntimeException(
                    "Không thể tải ảnh sản phẩm \"" + product.getProductName() + "\". Vui lòng thử sản phẩm khác.");
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  PROMPT BUILDER
    // ─────────────────────────────────────────────────────────────

    /**
     * Build a secure, highly descriptive prompt from real product data.
     * Sanitizes all DB strings to prevent prompt injection.
     */
    private String buildSecurePrompt(ProductDetailResponse product) {
        String name = sanitize(product.getProductName());
        String category = sanitize(product.getCategoryName());

        StringBuilder sb = new StringBuilder();
        sb.append("You are given two images. ");
        sb.append("Image 1 is a photo of a person. ");
        sb.append("Image 2 is a photo of a clothing product called \"").append(name).append("\"");

        if (!category.isEmpty()) {
            sb.append(" in the category \"").append(category).append("\"");
        }

        // Extract colors from variants
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            String colors = product.getVariants().stream()
                    .map(VariantDetailResponse::getColor)
                    .filter(c -> c != null && !c.isBlank())
                    .map(this::sanitize)
                    .distinct()
                    .collect(Collectors.joining(", "));
            if (!colors.isEmpty()) {
                sb.append(" with color: ").append(colors);
            }
        }

        sb.append(". ");

        // Description excerpt (max 150 chars, sanitized)
        if (product.getDescription() != null && !product.getDescription().isBlank()) {
            String desc = sanitize(product.getDescription());
            if (desc.length() > 150) {
                desc = desc.substring(0, 150);
            }
            sb.append("Product description: ").append(desc).append(". ");
        }

        sb.append("TASK: Generate a photorealistic image of the SAME person from Image 1 ");
        sb.append("wearing the EXACT clothing item shown in Image 2. ");
        sb.append("Requirements: ");
        sb.append("1) The clothing MUST match Image 2 exactly in color, pattern, style, and fit. ");
        sb.append("2) Keep the person's face, hair, skin tone, glasses, and body proportions IDENTICAL to Image 1. ");
        sb.append("3) Keep the original background from Image 1. ");
        sb.append("4) The clothing should fit naturally on the person's body. ");
        sb.append("5) Professional fashion photography, natural lighting, high quality.");

        return sb.toString();
    }

    /**
     * Sanitize user/DB input to prevent prompt injection.
     * Removes control characters and limits length.
     */
    private String sanitize(String input) {
        if (input == null) return "";
        // Remove newlines, tabs, and other control characters that could inject prompt instructions
        return input.replaceAll("[\\r\\n\\t]", " ")
                     .replaceAll("[^\\p{Print}\\p{IsLatin}\\p{IsHan}\\p{IsHiragana}\\p{IsKatakana}\\p{IsCyrillic}àáảãạăắằẳẵặâấầẩẫậèéẻẽẹêếềểễệìíỉĩịòóỏõọôốồổỗộơớờởỡợùúủũụưứừửữựỳýỷỹỵđ]", "")
                     .trim();
    }

    // ─────────────────────────────────────────────────────────────
    //  IMAGE PROCESSING
    // ─────────────────────────────────────────────────────────────

    /**
     * Resize any image to exactly 1024x1024 PNG, maintaining aspect ratio,
     * centered on a white background (no transparency issues).
     */
    private byte[] resizeToPng(byte[] imageBytes, String label) throws IOException {
        BufferedImage original = ImageIO.read(new ByteArrayInputStream(imageBytes));
        if (original == null) {
            throw new IOException("Cannot decode " + label + " image");
        }

        BufferedImage squared = new BufferedImage(TARGET_SIZE, TARGET_SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = squared.createGraphics();

        // White background
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, TARGET_SIZE, TARGET_SIZE);

        // Scale to fit
        double scale = Math.min((double) TARGET_SIZE / original.getWidth(),
                                (double) TARGET_SIZE / original.getHeight());
        int w = (int) (original.getWidth() * scale);
        int h = (int) (original.getHeight() * scale);
        int x = (TARGET_SIZE - w) / 2;
        int y = (TARGET_SIZE - h) / 2;

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.drawImage(original, x, y, w, h, null);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(squared, "png", baos);
        log.debug("Resized {} image: {}x{} -> {}x{} ({} bytes)",
                label, original.getWidth(), original.getHeight(), TARGET_SIZE, TARGET_SIZE, baos.size());
        return baos.toByteArray();
    }

    // ─────────────────────────────────────────────────────────────
    //  OPENAI API CALL
    // ─────────────────────────────────────────────────────────────

    /**
     * Call GPT Image 1 edits API with 2 images (user + product).
     * Uses image[] multipart fields as per OpenAI documentation.
     */
    @SuppressWarnings("unchecked")
    private String callGptImage1(byte[] userImagePng, byte[] productImagePng, String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.setBearerAuth(openAiApiKey);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        // Image 1: user's portrait
        body.add("image[]", new ByteArrayResource(userImagePng) {
            @Override public String getFilename() { return "user_portrait.png"; }
        });

        // Image 2: product clothing
        body.add("image[]", new ByteArrayResource(productImagePng) {
            @Override public String getFilename() { return "product_clothing.png"; }
        });

        body.add("prompt", prompt);
        body.add("model", "gpt-image-1");
        body.add("size", "1024x1024");
        body.add("quality", "high");
        body.add("n", 1);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            log.info("Calling GPT Image 1 API...");
            ResponseEntity<Map> response = restTemplate.exchange(
                    OPENAI_EDIT_URL, HttpMethod.POST, requestEntity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null) {
                throw new RuntimeException("Empty response from AI service");
            }

            // GPT Image 1 may return base64 data or URL
            if (responseBody.containsKey("data")) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) responseBody.get("data");
                if (data != null && !data.isEmpty()) {
                    Map<String, Object> firstResult = data.get(0);

                    // Check for URL first
                    if (firstResult.containsKey("url") && firstResult.get("url") != null) {
                        String url = firstResult.get("url").toString();
                        log.info("GPT Image 1 returned URL result");
                        return url;
                    }

                    // Check for base64 data
                    if (firstResult.containsKey("b64_json") && firstResult.get("b64_json") != null) {
                        String b64 = firstResult.get("b64_json").toString();
                        log.info("GPT Image 1 returned base64 result ({} chars)", b64.length());
                        return "data:image/png;base64," + b64;
                    }
                }
            }

            log.error("Unexpected API response structure: {}", responseBody.keySet());
            throw new RuntimeException("Không thể xử lý phản hồi từ AI. Vui lòng thử lại.");

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            log.error("OpenAI API error {}: {}", e.getStatusCode(), errorBody);

            if (e.getStatusCode().value() == 429) {
                throw new RuntimeException("Hệ thống AI đang quá tải. Vui lòng thử lại sau vài phút.");
            }
            if (e.getStatusCode().value() == 401) {
                throw new RuntimeException("API key không hợp lệ. Vui lòng liên hệ quản trị viên.");
            }
            if (e.getStatusCode().value() == 400) {
                throw new RuntimeException("Yêu cầu không hợp lệ. Vui lòng thử ảnh khác hoặc sản phẩm khác.");
            }
            throw new RuntimeException("Lỗi từ dịch vụ AI (HTTP " + e.getStatusCode().value() + "). Vui lòng thử lại.");
        } catch (org.springframework.web.client.ResourceAccessException e) {
            log.error("OpenAI API timeout/connection error: {}", e.getMessage());
            throw new RuntimeException("Kết nối đến AI bị timeout. Vui lòng thử lại.");
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error calling OpenAI: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi không xác định khi gọi AI. Vui lòng thử lại.");
        }
    }
}
