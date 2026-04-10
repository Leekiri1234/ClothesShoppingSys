from pathlib import Path
path = Path('com.clothshop/shop-api-admin/src/main/resources/templates/admin/dashboard.html')
for i,line in enumerate(path.read_text(encoding='utf-8').splitlines(), start=1):
    if 'revenue-list' in line:
        print('revenue-list at', i)
    if 'top-products-list' in line:
        print('top-products-list at', i)
