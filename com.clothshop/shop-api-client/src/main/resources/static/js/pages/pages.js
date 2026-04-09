// highlight row size
document.querySelectorAll(".size-table tbody tr").forEach(row => {
    row.addEventListener("click", () => {
        document.querySelectorAll(".size-table tbody tr")
            .forEach(r => r.classList.remove("active"));
        row.classList.add("active");
    });
});