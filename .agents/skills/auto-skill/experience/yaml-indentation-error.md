# Lỗi URL MoMo trả về null (URI with undefined scheme) do sai cấu trúc thụt lề YAML

## Nguyên nhân
Trong file `application.yaml`, cấu trúc của cấu hình `payment.momo` đã bị thụt lề (indentation) nhầm, trở thành con của `payment.vnpay`:
```yaml
payment:
  vnpay:
    url: ...
    momo: # ❌ LỖI: momo đang nằm trong vnpay
      endpoint: https://test-payment.momo.vn/v2/gateway/api/create
```
Điều này dẫn đến việc Spring Boot đọc sai đường dẫn property. Thay vì đọc từ `payment.momo.endpoint` (như `@ConfigurationProperties(prefix = "payment.momo")` yêu cầu), nó lại hiểu thành `payment.vnpay.momo.endpoint`.
Hậu quả là các biến trong `MomoConfig` đều mang giá trị `null`. Khi gọi API qua `RestClient` với url là `null`, Java sẽ ném ra lỗi `java.lang.IllegalArgumentException: URI with undefined scheme`.

## Cách giải quyết
Đưa khối cấu hình `momo:` ra thụt lề cùng cấp với khối `vnpay:`:
```yaml
payment:
  vnpay:
    url: ...
  momo: # ✅ ĐÚNG: momo nằm ngang hàng vnpay, trực thuộc payment
    endpoint: https://test-payment.momo.vn/v2/gateway/api/create
```
