# Thiết kế UI/UX (Desktop-first) – ChatBox + ProductModal (USER đã đăng nhập)

## Global Styles

- Tokens: Primary `#2563EB`, Border `#E5E7EB`, Text `#111827`, Muted `#6B7280`, Surface `#FFFFFF`
- Bubble USER: `#F3F4F6`, Bubble ADMIN: `#DBEAFE`
- Typography: base 14–16px; timestamp 12px; tiêu đề 14–16px semibold
- States: skeleton khi load lịch sử; toast khi WS mất kết nối hoặc upload lỗi

---

## 1) ChatBox.vue (Fixed widget)

### Kích thước & vị trí

- Fixed góc phải dưới
- Size mặc định: 340x450px
- `isOpen`: đóng/mở widget
- `isMinimized`: thu gọn chỉ còn header (cao ~48px)

### Header (Product Context)

- Hiển thị: tên sản phẩm + (tuỳ chọn) thumbnail nhỏ 28–32px
- Subline: `Mã SP: {productId}` hoặc giá (nếu có sẵn)
- Actions:
  - Nút “Thay đổi” mở `ProductModal`
  - Nút minimize/restore
  - Nút close

### Message List

- Scroll area chiếm phần lớn chiều cao; auto-scroll khi có tin mới (trừ khi user đang cuộn lên)
- Bubble theo vai trò:
  - USER: canh phải
  - ADMIN: canh trái
- Nội dung:
  - Text (xuống dòng giữ định dạng)
  - Ảnh: hiển thị preview; click mở full (tuỳ chọn)
- Timestamp dưới bubble (12px, muted)

### Composer

- Textarea 2–4 dòng
- Hotkeys: Enter gửi, Shift+Enter xuống dòng
- Buttons:
  - Gửi
  - Đính kèm ảnh (input file ẩn)
  - Camera (mở quyền `navigator.mediaDevices.getUserMedia` và chụp ảnh)
- Upload:
  - Khi chọn ảnh/camera: upload REST lấy `mediaUrl`, sau đó gửi message WS chứa `mediaUrl`
  - Hiển thị trạng thái “Đang tải ảnh…” và disable nút gửi trong lúc upload

### Connection & Empty states

- Banner mảnh phía trên composer:
  - “Đang kết nối…” / “Đang kết nối lại…”
  - “Không thể kết nối, vui lòng thử lại”
- Empty state: “Chọn sản phẩm để bắt đầu nhắn tin” khi chưa có `productId`

---

## 2) ProductModal.vue (Chọn sản phẩm)

### Layout

- Modal centered, rộng 720–840px (desktop)
- Header: “Chọn sản phẩm để hỗ trợ” + nút đóng
- Body: Tabs + danh sách sản phẩm

### Tabs

1) **Giỏ hàng**
- Gọi API cart
- List item: thumbnail + tên + giá/variant (nếu có) + nút “Chọn”

2) **Đã mua**
- Gọi API đơn hàng trạng thái `DELIVERED_SUCCESS`
- List item tương tự

### Event

- Khi chọn sản phẩm: emit `change-product` với `{ productId, productName, thumbnailUrl? }` và đóng modal

---

## 3) Trạng thái khóa chat (ADMIN UI)

Khi một Admin cố gửi tin vào luồng (customer_id, product_id) đã được Admin khác “nhận hỗ trợ”, UI Admin cần:

- Disable composer
- Hiển thị banner: “Tin nhắn này đã được nhận hỗ trợ bởi Admin {Tên}”
- Không tự động đóng khung chat; vẫn cho phép xem lịch sử
