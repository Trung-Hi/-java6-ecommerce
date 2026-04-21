# FinalAsignment_Java6

## Cấu hình đăng nhập Google (OAuth2)

Project dùng Spring Security OAuth2 (Authorization Code) với endpoint bắt đầu đăng nhập:

- `GET /oauth2/authorization/google`

Callback mặc định của Spring Security:

- `GET /login/oauth2/code/google`

### Lỗi `Error 401: deleted_client`

Đây không phải lỗi code; nó xảy ra khi `client_id` đang dùng đã bị xoá/disabled trên Google Cloud, hoặc bạn đang chạy với bộ `GOOGLE_CLIENT_ID/GOOGLE_CLIENT_SECRET` cũ.

Chỗ cấu hình trong project:

- [application.yml](file:///Users/tranminh/FPOLY/5.SPRING26/BLOCK2/JAVA6/FinalAsignment_Java6/src/main/resources/application.yml) đọc từ env:
  - `GOOGLE_CLIENT_ID`
  - `GOOGLE_CLIENT_SECRET`

### Cách sửa

1) Tạo lại OAuth Client ID trên Google Cloud Console

- APIs & Services → Credentials → Create Credentials → OAuth client ID
- Application type: Web application
- Authorized redirect URIs (local):
  - `http://localhost:8080/login/oauth2/code/google`

Nếu deploy domain thật, thêm đúng domain:

- `https://<domain>/login/oauth2/code/google`

2) Cập nhật env khi chạy backend

- Set `GOOGLE_CLIENT_ID` và `GOOGLE_CLIENT_SECRET` theo bộ credential mới.
- Khuyến nghị tạo file `.env` từ `.env.example` (không commit secrets).

### Kiểm tra nhanh

Mở link `http://localhost:8080/oauth2/authorization/google` và nhìn URL Google trả về (trên thanh địa chỉ) có query `client_id=...`.
`client_id` phải trùng với credential mới vừa tạo.
