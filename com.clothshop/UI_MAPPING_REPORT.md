# UI Mapping Report

## 1) Nguon UI duoc boc tach
- Client legacy source: `shop-api-client/src/main/resources/static/css/legacy-client.css` + `shop-api-client/src/main/resources/static/js/legacy-client.js`
- Admin legacy source: `shop-api-admin/src/main/resources/static/css/legacy-admin.css` + `shop-api-admin/src/main/resources/static/js/legacy-admin.js`
- Muc tieu: UI khong phu thuoc `_legacy/`, toan bo nam trong resources cua tung module.

## 2) Bug fix bat buoc da lam
- Da bo toan bo bieu thuc `#httpServletRequest.requestURI` trong template (32 vi tri), tranh loi parse Thymeleaf khi render route.

## 3) Mapping da noi vao data backend that
### Client
- `templates/client/home.html`
  - Hero + thong ke nhanh + list san pham noi bat (`featuredProducts`) + list bo suu tap (`homeCollections`).
- `templates/client/products/list.html`
  - Bo loc tim kiem + category + card san pham + phan trang (dua tren `Page<ProductListResponse>`).
- `templates/client/collections/list.html` (moi)
  - List bo suu tap active (`collections`) + link vao detail theo slug.
- `templates/client/collections/detail.html`
  - Mo ta bo suu tap + list san pham thuoc collection (`collection.products`).

### Admin
- `templates/admin/orders/list.html` (moi)
  - Filter keyword/status/startDate + bang danh sach don + phan trang (`Page<OrderAdminResponse>`).
- `templates/admin/orders/detail.html` (moi)
  - Thong tin don + danh sach item + lich su status + form update trang thai.

## 4) Backend route da co, frontend da bo sung file thieu
- Da bo sung:
  - `admin/orders/list`
  - `admin/orders/detail`
  - `client/collections/list`

## 5) Backend route da co, frontend con placeholder (can map tiep)
### Client
- `templates/client/cart/view.html`
- `templates/client/checkout/form.html`
- `templates/client/orders/list.html`
- `templates/client/orders/detail.html`
- `templates/client/products/detail.html`
- `templates/client/products/category.html`
- `templates/client/profile/view.html`
- `templates/client/profile/edit.html`
- `templates/client/vouchers/inventory.html`

### Admin
- `templates/admin/dashboard.html`
- `templates/admin/products/list.html`
- `templates/admin/products/create.html`
- `templates/admin/products/edit.html`
- `templates/admin/products/detail.html`
- `templates/admin/products/variants/create.html`
- `templates/admin/products/variants/update-price.html`
- `templates/admin/products/variants/update-stock.html`
- `templates/admin/categories/list.html`
- `templates/admin/categories/create.html`
- `templates/admin/categories/edit.html`
- `templates/admin/collections/list.html`
- `templates/admin/collections/form.html`
- `templates/admin/collections/assign-products.html`
- `templates/admin/vouchers/list.html`
- `templates/admin/vouchers/form.html`
- `templates/admin/staff/list.html`
- `templates/admin/staff/create.html`
- `templates/admin/staff/edit.html`
- `templates/admin/settings/featured.html`

## 6) Ghep chuc nang legacy -> template hien tai (muc uu tien)
### Client legacy blocks (`legacy-client.js`)
- Home sections (hero, featured products, collections, flash-sale):
  - Da map mot phan: home + featured products + collections.
  - Chua map: flash-sale countdown, carousel block.
- Product listing/filter/sort/pagination:
  - Da map list + filter co ban + pagination server-side.
  - Chua map: filter theo color/special badge nhu legacy JS mock.
- Cart/checkout/orders/profile/wishlist/returns/try-on:
  - Chua map day du vao templates server-side.

### Admin legacy blocks (`legacy-admin.js`)
- Orders:
  - Da map list + detail + update status.
- Dashboard/products/categories/collections/vouchers/staff/settings:
  - Chua map day du, dang o placeholder.

## 7) Next step de hoan tat migration UI
1. Map client `products/detail`, `cart/view`, `checkout/form`, `orders/*` theo data that tu controller/service.
2. Map admin `dashboard` va `products/*` truoc (do la luong su dung chinh).
3. Chuyen cac thanh phan lap lai thanh fragment (card, table, form actions) de de bao tri.
4. Sau khi map xong, co the xoa han `_legacy/` ma khong anh huong UI runtime.
