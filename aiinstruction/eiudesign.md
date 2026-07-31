# EIU-INSPIRED WEB DESIGN LANGUAGE SPECIFICATION

**Document type:** UI Design System Reference
**Version:** 1.0
**Target platform:** Responsive Web Application
**Primary users:** UI/UX Designer, Frontend Developer, AI Coding Assistant
**Reference direction:** Eastern International University website visual language

---

## 1. DESIGN INTENT

### 1.1. Mục tiêu

Hệ thống giao diện phải truyền tải đồng thời bốn đặc tính:

| Thuộc tính   | Ý nghĩa thiết kế                                                  |
| ------------ | ----------------------------------------------------------------- |
| Academic     | Thể hiện sự đáng tin cậy, có tổ chức và chuyên nghiệp             |
| Modern       | Giao diện sạch, rõ ràng, phù hợp với sản phẩm số hiện đại         |
| Accessible   | Dễ đọc, dễ điều hướng, không sử dụng hiệu ứng gây nhiễu           |
| Aspirational | Hình ảnh và nội dung tạo cảm giác phát triển, thành tựu và cơ hội |

Thiết kế không được mang cảm giác quá trẻ con, quá công nghệ, quá thương mại hoặc giống một dashboard SaaS thông thường.

### 1.2. Tính cách thương hiệu

| Thuộc tính mong muốn | Tránh                    |
| -------------------- | ------------------------ |
| Chuyên nghiệp        | Khô cứng                 |
| Hiện đại             | Quá tối giản và lạnh lẽo |
| Đáng tin cậy         | Nặng nề, hành chính      |
| Năng động            | Màu sắc hỗn loạn         |
| Thân thiện           | Trẻ con                  |
| Có uy tín            | Phô trương               |

### 1.3. Nguyên tắc cốt lõi

1. **Clarity before decoration:** Thông tin và hành động phải rõ ràng trước khi thêm hiệu ứng.
2. **Strong visual hierarchy:** Mỗi màn hình phải có một điểm tập trung chính.
3. **Whitespace is structural:** Khoảng trắng được dùng để phân nhóm, không phải khoảng trống thừa.
4. **Photography carries emotion:** Ảnh thật chịu trách nhiệm tạo cảm xúc; UI giữ vai trò tổ chức.
5. **Blue establishes authority:** Màu xanh là màu nhận diện chính, không dùng tùy tiện như màu trang trí.
6. **Gold is an accent:** Màu vàng chỉ dùng để tạo điểm nhấn, không dùng làm màu nền chính cho diện tích lớn.
7. **Consistency over novelty:** Không sáng tạo một kiểu component mới nếu component hiện có đã giải quyết được vấn đề.

---

# 2. COLOR SYSTEM

## 2.1. Primary brand colors

| Token               |       HEX | Vai trò               | Cách sử dụng                                             |
| ------------------- | --------: | --------------------- | -------------------------------------------------------- |
| `brand-primary-900` | `#003B71` | Màu thương hiệu chính | Header, footer, tiêu đề quan trọng, nền section đặc biệt |
| `brand-primary-800` | `#00518C` | Primary action        | Nút chính, link nổi bật, trạng thái active               |
| `brand-primary-700` | `#0066A4` | Interactive accent    | Hover nhẹ, icon, link phụ                                |
| `brand-primary-100` | `#EAF4FA` | Brand surface         | Nền thông tin, badge nhẹ, section phụ                    |
| `brand-accent-500`  | `#F2B233` | Accent chính          | Đường nhấn, icon chọn lọc, highlight nhỏ                 |
| `brand-accent-600`  | `#D99A1A` | Accent đậm            | Hover hoặc trạng thái nhấn mạnh của accent               |

## 2.2. Neutral colors

| Token           |       HEX | Vai trò                                    |
| --------------- | --------: | ------------------------------------------ |
| `neutral-white` | `#FFFFFF` | Nền chính                                  |
| `neutral-50`    | `#F7F8FA` | Nền section xen kẽ                         |
| `neutral-100`   | `#EEF1F4` | Nền disabled hoặc subtle surface           |
| `neutral-200`   | `#E2E7EB` | Divider nhẹ                                |
| `neutral-300`   | `#D3D9DF` | Border mặc định                            |
| `neutral-500`   | `#84909A` | Placeholder, icon phụ                      |
| `neutral-600`   | `#66717D` | Nội dung phụ                               |
| `neutral-800`   | `#263238` | Nội dung chính                             |
| `neutral-900`   | `#111820` | Tiêu đề hoặc nội dung có độ tương phản cao |

## 2.3. Semantic colors

| Token         |       HEX | Vai trò                  |
| ------------- | --------: | ------------------------ |
| `success-600` | `#2E7D32` | Hoàn thành, thành công   |
| `success-100` | `#E8F5E9` | Nền success              |
| `warning-600` | `#B7791F` | Cảnh báo                 |
| `warning-100` | `#FFF8E1` | Nền warning              |
| `danger-600`  | `#C62828` | Lỗi, hành động nguy hiểm |
| `danger-100`  | `#FDECEC` | Nền lỗi                  |
| `info-600`    | `#0277BD` | Thông tin                |
| `info-100`    | `#E6F4FB` | Nền thông tin            |

## 2.4. Quy tắc sử dụng màu

### Primary blue

Được phép sử dụng cho:

* Primary button
* Header và footer
* Tiêu đề section quan trọng
* Link chính
* Icon quan trọng
* Selected navigation item
* Highlighted statistics

Không sử dụng primary blue cho tất cả card, tất cả icon hoặc toàn bộ đoạn văn.

### Gold accent

Được phép sử dụng cho:

* Đường gạch ngắn dưới tiêu đề
* Badge hoặc label đặc biệt
* Icon nhỏ mang tính thương hiệu
* Chi tiết trang trí giới hạn
* Hover accent trong một số khu vực marketing

Không sử dụng màu vàng cho:

* Đoạn văn dài
* Nút chính diện tích lớn
* Background toàn màn hình
* Border của tất cả component
* Quá ba điểm nhấn đồng thời trong cùng viewport

### Color ratio gợi ý

Trong một màn hình thông thường:

| Nhóm màu                 | Tỷ lệ thị giác |
| ------------------------ | -------------: |
| White và neutral surface |         65–75% |
| Text và neutral dark     |         15–20% |
| Primary blue             |          8–15% |
| Gold accent              |        Dưới 5% |

---

# 3. TYPOGRAPHY SYSTEM

## 3.1. Font family

### Preferred configuration

```css
--font-heading: "Montserrat", "Arial", sans-serif;
--font-body: "Inter", "Arial", sans-serif;
```

### Fallback configuration

Nếu không thể sử dụng hai font:

```css
--font-heading: "Montserrat", sans-serif;
--font-body: "Montserrat", sans-serif;
```

### Vai trò

| Font       | Sử dụng                                       |
| ---------- | --------------------------------------------- |
| Montserrat | Heading, navigation, button, number highlight |
| Inter      | Body text, form, table, description, metadata |

Không sử dụng quá hai font family trong cùng sản phẩm.

## 3.2. Font weight

| Token       | Weight | Sử dụng                        |
| ----------- | -----: | ------------------------------ |
| `regular`   |    400 | Body text                      |
| `medium`    |    500 | Label, metadata quan trọng     |
| `semibold`  |    600 | Navigation, card title         |
| `bold`      |    700 | Page title, section title, CTA |
| `extrabold` |    800 | Chỉ dùng rất hạn chế cho hero  |

Không sử dụng `font-weight: 900` trừ khi có yêu cầu thương hiệu rõ ràng.

## 3.3. Type scale

| Style                 | Desktop | Mobile | Line height | Weight |
| --------------------- | ------: | -----: | ----------: | -----: |
| Display / Hero        |    56px |   36px |        1.08 |    700 |
| H1 / Page title       |    44px |   32px |        1.15 |    700 |
| H2 / Section title    |    36px |   28px |        1.20 |    700 |
| H3 / Subsection       |    28px |   24px |        1.25 |    600 |
| H4 / Card title large |    22px |   20px |        1.35 |    600 |
| H5 / Card title       |    18px |   18px |        1.40 |    600 |
| Body large            |    18px |   17px |        1.65 |    400 |
| Body                  |    16px |   16px |        1.65 |    400 |
| Body small            |    14px |   14px |        1.55 |    400 |
| Caption               |    12px |   12px |        1.45 |    500 |
| Button                |    14px |   14px |        1.20 |    700 |
| Navigation            |    14px |   14px |        1.20 |    600 |

## 3.4. Typography rules

* Độ dài lý tưởng của đoạn văn: **55–75 ký tự mỗi dòng**.
* Không căn giữa body text dài.
* Tiêu đề có thể căn giữa trong landing page nhưng không áp dụng cho trang quản trị hoặc trang nội dung dài.
* Không viết hoa toàn bộ đoạn văn.
* Uppercase chỉ dùng cho navigation, label ngắn hoặc eyebrow text.
* Letter spacing của uppercase text: `0.02em–0.06em`.
* Tiêu đề không nên dài quá ba dòng.
* Card title nên giới hạn ở hai dòng.
* Paragraph không sử dụng bold cho toàn bộ nội dung.

---

# 4. SPACING SYSTEM

## 4.1. Base unit

Hệ thống sử dụng **4px base unit**.

Mọi khoảng cách nên là bội số của 4, ưu tiên các giá trị trong scale sau:

| Token      | Giá trị |
| ---------- | ------: |
| `space-1`  |     4px |
| `space-2`  |     8px |
| `space-3`  |    12px |
| `space-4`  |    16px |
| `space-5`  |    20px |
| `space-6`  |    24px |
| `space-7`  |    32px |
| `space-8`  |    40px |
| `space-9`  |    48px |
| `space-10` |    64px |
| `space-11` |    80px |
| `space-12` |    96px |
| `space-13` |   120px |

## 4.2. Spacing rules

| Quan hệ                          | Khoảng cách đề xuất |
| -------------------------------- | ------------------: |
| Icon và label                    |                 8px |
| Label và input                   |                 8px |
| Các phần tử trong button         |                 8px |
| Title và description             |             12–16px |
| Các paragraph liên tiếp          |                16px |
| Các field trong form             |             20–24px |
| Nội dung bên trong card          |                24px |
| Các card trong grid              |             24–32px |
| Subsection                       |             40–48px |
| Section chính                    |             64–96px |
| Hero content với section kế tiếp |            80–120px |

Khoảng cách giữa hai nhóm không liên quan phải lớn hơn khoảng cách giữa các phần tử cùng nhóm.

---

# 5. LAYOUT SYSTEM

## 5.1. Container

```css
--container-sm: 720px;
--container-md: 960px;
--container-lg: 1200px;
--container-xl: 1280px;
```

Container mặc định:

```css
.container {
  width: min(calc(100% - 40px), 1280px);
  margin-inline: auto;
}
```

Mobile:

```css
@media (max-width: 767px) {
  .container {
    width: calc(100% - 32px);
  }
}
```

## 5.2. Grid

Desktop sử dụng lưới 12 cột.

| Breakpoint          | Số cột |  Gutter |
| ------------------- | -----: | ------: |
| Mobile `< 768px`    |      4 |    16px |
| Tablet `768–1023px` |      8 |    24px |
| Desktop `≥ 1024px`  |     12 | 24–32px |

## 5.3. Breakpoints

```css
--breakpoint-sm: 576px;
--breakpoint-md: 768px;
--breakpoint-lg: 1024px;
--breakpoint-xl: 1280px;
--breakpoint-2xl: 1440px;
```

## 5.4. Page composition

Một trang public-facing tiêu chuẩn nên có cấu trúc:

1. Utility bar nếu cần
2. Main header
3. Primary navigation
4. Hero hoặc page banner
5. Introductory section
6. Main content sections
7. Related content hoặc CTA
8. Footer

Một trang dashboard hoặc functional application nên có:

1. Application header
2. Sidebar hoặc top navigation
3. Page title và primary action
4. Filter hoặc summary area
5. Main working area
6. Feedback state
7. Optional secondary panel

Không sử dụng cấu trúc landing page cho màn hình tác vụ phức tạp.

---

# 6. SURFACE, BORDER, RADIUS AND SHADOW

## 6.1. Border radius

| Token         | Giá trị | Sử dụng                       |
| ------------- | ------: | ----------------------------- |
| `radius-xs`   |     2px | Indicator nhỏ                 |
| `radius-sm`   |     4px | Button, input, badge          |
| `radius-md`   |     8px | Card, dropdown                |
| `radius-lg`   |    12px | Modal, feature panel          |
| `radius-xl`   |    16px | Chỉ dùng cho section đặc biệt |
| `radius-full` |   999px | Avatar, pill badge            |

Phong cách chung là **moderately squared**. Không sử dụng bo góc 20–32px cho mọi component.

## 6.2. Borders

| Token          | Giá trị             |
| -------------- | ------------------- |
| Default border | `1px solid #DCE2E7` |
| Strong border  | `1px solid #B8C2CA` |
| Focus border   | `2px solid #0066A4` |
| Divider        | `1px solid #E2E7EB` |

Không dùng border đen hoặc border quá dày cho card thông thường.

## 6.3. Shadows

```css
--shadow-xs: 0 1px 2px rgba(13, 42, 65, 0.06);

--shadow-sm:
  0 4px 12px rgba(13, 42, 65, 0.08);

--shadow-md:
  0 8px 24px rgba(13, 42, 65, 0.10);

--shadow-lg:
  0 16px 40px rgba(13, 42, 65, 0.14);
```

| Shadow      | Sử dụng                       |
| ----------- | ----------------------------- |
| `shadow-xs` | Input nổi nhẹ, sticky divider |
| `shadow-sm` | Card mặc định                 |
| `shadow-md` | Card hover, dropdown          |
| `shadow-lg` | Modal hoặc floating panel     |

Không kết hợp shadow lớn với border đậm trong cùng component.

---

# 7. BUTTON SYSTEM

## 7.1. Button hierarchy

| Loại        | Vai trò                                |
| ----------- | -------------------------------------- |
| Primary     | Hành động chính duy nhất trong khu vực |
| Secondary   | Hành động quan trọng nhưng không chính |
| Tertiary    | Hành động nhẹ hoặc inline action       |
| Destructive | Xóa hoặc hành động không thể hoàn tác  |
| Icon button | Hành động ngắn gọn bằng icon           |

## 7.2. Primary button

```css
.button-primary {
  min-height: 46px;
  padding: 12px 24px;
  border: 1px solid transparent;
  border-radius: 4px;
  background: #00518C;
  color: #FFFFFF;

  font-family: var(--font-heading);
  font-size: 14px;
  font-weight: 700;
  line-height: 1.2;
}
```

### States

| State    | Style                                    |
| -------- | ---------------------------------------- |
| Default  | `#00518C`                                |
| Hover    | `#003B71`                                |
| Active   | `#002D57`                                |
| Focus    | Focus ring xanh nhạt 3px                 |
| Disabled | Nền `#D3D9DF`, chữ `#84909A`             |
| Loading  | Giữ nguyên chiều rộng, thay icon spinner |

## 7.3. Secondary button

* Nền trắng hoặc transparent.
* Border `#00518C`.
* Text `#00518C`.
* Hover dùng nền `#EAF4FA`.

## 7.4. Button rules

* Một section chỉ nên có một primary button.
* Hai button cạnh nhau phải theo thứ tự: primary trước, secondary sau.
* Button label bắt đầu bằng động từ: “Xem chương trình”, “Đăng ký”, “Lưu thay đổi”.
* Không dùng câu dài hơn bốn đến năm từ.
* Không thay đổi chiều rộng button khi chuyển sang loading.
* Không chỉ dùng màu để phân biệt trạng thái.

---

# 8. CARD SYSTEM

## 8.1. Base card

```css
.card {
  background: #FFFFFF;
  border: 1px solid #E2E7EB;
  border-radius: 8px;
  overflow: hidden;
}
```

## 8.2. Card anatomy

1. Optional media
2. Optional eyebrow hoặc category
3. Title
4. Description
5. Metadata
6. Optional action

## 8.3. Card spacing

| Thành phần               |            Khoảng cách |
| ------------------------ | ---------------------: |
| Card padding             |                   24px |
| Media đến body           | 0 nếu media full-width |
| Eyebrow đến title        |                    8px |
| Title đến description    |                   12px |
| Description đến metadata |                   16px |
| Nội dung đến action      |                20–24px |

## 8.4. Card variants

### Content card

Dùng cho tin tức, chương trình đào tạo, sự kiện.

* Có ảnh tỷ lệ `16:10` hoặc `4:3`.
* Title tối đa hai dòng.
* Description tối đa ba dòng trong grid.
* Hover nâng card `translateY(-4px)`.
* Không scale ảnh quá mạnh.

### Information card

Dùng cho thông tin tĩnh hoặc thống kê.

* Có thể không có ảnh.
* Icon đặt phía trên hoặc bên trái.
* Không dùng nhiều màu nền khác nhau.

### Action card

Dùng khi toàn bộ card có thể click.

* Toàn bộ card là một vùng tương tác.
* Có hover và focus rõ ràng.
* Không đặt nhiều button bên trong card clickable.

## 8.5. Card restrictions

Không được:

* Trộn ba kiểu radius khác nhau trong cùng grid.
* Dùng một màu nền khác nhau cho mỗi card.
* Dùng nhiều hơn một shadow level trong cùng danh sách.
* Căn giữa toàn bộ nội dung card khi nội dung dài.
* Đặt quá nhiều icon trang trí.

---

# 9. IMAGE AND MEDIA LANGUAGE

## 9.1. Photography direction

Ưu tiên:

* Ảnh thật
* Ánh sáng tự nhiên
* Con người trong bối cảnh hoạt động
* Campus, lớp học, phòng lab, sự kiện
* Khoảnh khắc có tương tác
* Hình ảnh có chiều sâu và không gian

Tránh:

* Stock photo mang tính minh họa chung chung
* Pose quá cứng
* Background quá hỗn loạn
* Filter màu quá mạnh
* Ảnh chất lượng thấp
* Ảnh AI có lỗi hình thể hoặc biểu cảm

## 9.2. Image ratios

| Mục đích       | Tỷ lệ                                        |
| -------------- | -------------------------------------------- |
| Hero desktop   | `16:7` đến `16:9`                            |
| Feature image  | `16:10`                                      |
| News card      | `4:3` hoặc `16:10`                           |
| Profile        | `1:1`                                        |
| Story portrait | `3:4`                                        |
| Gallery        | Duy trì một tỷ lệ thống nhất trong từng hàng |

## 9.3. Image overlays

Khi đặt text trên ảnh:

* Thêm overlay tối `rgba(0, 24, 48, 0.45–0.65)`.
* Text phải là trắng.
* Không đặt text trên khu vực ảnh quá chi tiết.
* Chiều rộng text không vượt quá 600–680px.
* Không dùng quá hai CTA trên hero.

---

# 10. ICONOGRAPHY

## 10.1. Style

* Dùng outline icon hoặc solid icon có trọng lượng vừa phải.
* Tất cả icon trong cùng màn hình phải đến từ một icon family.
* Stroke width nên đồng nhất.
* Kích thước phổ biến: 16px, 20px, 24px và 32px.

## 10.2. Icon usage

| Kích thước | Sử dụng                         |
| ---------- | ------------------------------- |
| 16px       | Input, table, metadata          |
| 20px       | Button, navigation              |
| 24px       | Standalone action               |
| 32px       | Feature card                    |
| 40–48px    | Marketing illustration đơn giản |

Không dùng icon chỉ để lấp khoảng trống.

Icon không được thay thế label trong các hành động không phổ biến.

---

# 11. FORM SYSTEM

## 11.1. Input dimensions

| Size    |  Height |
| ------- | ------: |
| Small   |    36px |
| Default | 44–46px |
| Large   |    52px |

Input mặc định:

```css
.input {
  min-height: 46px;
  padding: 10px 14px;
  border: 1px solid #D3D9DF;
  border-radius: 4px;
  background: #FFFFFF;
  color: #263238;
  font-size: 16px;
}
```

## 11.2. Label

* Font size: 14px.
* Font weight: 600.
* Màu: `neutral-800`.
* Khoảng cách dưới label: 8px.
* Required indicator dùng dấu `*`, màu danger.

## 11.3. Form states

| State    | Style                             |
| -------- | --------------------------------- |
| Default  | Border neutral                    |
| Hover    | Border `#9EABB5`                  |
| Focus    | Border primary và focus ring      |
| Error    | Border danger, helper text danger |
| Success  | Chỉ dùng khi xác nhận có giá trị  |
| Disabled | Nền neutral-100, text neutral-500 |

## 11.4. Validation rules

* Error message đặt ngay dưới field.
* Error message phải giải thích cách sửa.
* Không chỉ hiển thị “Invalid input”.
* Không xóa dữ liệu người dùng sau validation.
* Không báo lỗi trước khi người dùng tương tác, trừ lỗi từ server sau submit.

---

# 12. NAVIGATION

## 12.1. Main navigation

* Navigation desktop sử dụng font 14px, semibold.
* Chiều cao header khoảng 72–88px.
* Active item phải có ít nhất hai dấu hiệu: màu và underline/background.
* Dropdown phải căn theo item kích hoạt.
* Không có quá bảy mục navigation cấp cao nhất.

## 12.2. Mobile navigation

* Sử dụng menu drawer hoặc full-screen navigation.
* Vùng bấm tối thiểu 44×44px.
* Submenu có trạng thái mở/đóng rõ ràng.
* Không phụ thuộc vào hover.
* Nút đóng phải luôn hiển thị.

## 12.3. Breadcrumb

Sử dụng breadcrumb cho trang có độ sâu từ hai cấp trở lên.

* Font size 13–14px.
* Màu neutral-600.
* Trang hiện tại không click.
* Không hiển thị breadcrumb trong hero nếu gây quá tải thị giác.

---

# 13. TABLE AND DATA DISPLAY

Dù định hướng thiết kế xuất phát từ website học thuật, các màn hình quản trị vẫn phải duy trì cùng hệ thống màu và typography.

## 13.1. Table styling

* Header background: `neutral-50`.
* Header font: 13–14px, semibold.
* Row text: 14px.
* Row height: 48–56px.
* Divider nhẹ giữa các hàng.
* Hover row dùng `brand-primary-100` ở cường độ rất nhẹ.
* Số liệu căn phải.
* Text căn trái.
* Status căn trái hoặc giữa tùy cấu trúc.

## 13.2. Status badge

| Status             | Style                             |
| ------------------ | --------------------------------- |
| Active / Completed | Success background + success text |
| Pending            | Warning background + warning text |
| Failed             | Danger background + danger text   |
| Draft / Neutral    | Neutral background + neutral text |
| In progress        | Info background + info text       |

Badge không dùng màu quá bão hòa và không viết hoa toàn bộ nếu label dài.

---

# 14. MOTION AND INTERACTION

## 14.1. Duration

| Loại                  |     Duration |
| --------------------- | -----------: |
| Hover nhỏ             |    120–180ms |
| Component state       |    180–220ms |
| Dropdown              |    180–240ms |
| Modal                 |    220–300ms |
| Page-level transition | Tối đa 350ms |

## 14.2. Easing

```css
--ease-standard: cubic-bezier(0.2, 0, 0, 1);
--ease-enter: cubic-bezier(0, 0, 0, 1);
--ease-exit: cubic-bezier(0.3, 0, 1, 1);
```

## 14.3. Motion rules

* Motion phải giải thích sự thay đổi trạng thái.
* Không dùng animation liên tục cho thành phần không cần chú ý.
* Hover card chỉ dịch chuyển 2–4px.
* Không scale button quá mức.
* Tôn trọng `prefers-reduced-motion`.
* Không dùng parallax mạnh trên mobile.

---

# 15. ACCESSIBILITY REQUIREMENTS

## 15.1. Contrast

* Body text phải đạt contrast tối thiểu 4.5:1.
* Large text phải đạt tối thiểu 3:1.
* Interactive component và border quan trọng phải đạt tối thiểu 3:1.
* Không đặt chữ vàng trên nền trắng cho nội dung cần đọc.

## 15.2. Interaction

* Mọi chức năng phải sử dụng được bằng keyboard.
* Focus state phải luôn hiển thị.
* Touch target tối thiểu 44×44px.
* Không sử dụng hover làm cách duy nhất để mở nội dung quan trọng.
* Icon-only button phải có accessible label.
* Form field phải liên kết với label.

## 15.3. Content

* Alt text mô tả mục đích của hình ảnh.
* Hình ảnh trang trí dùng alt rỗng.
* Heading phải theo thứ tự logic.
* Không bỏ qua cấp heading chỉ vì mục đích tạo kiểu.

---

# 16. RESPONSIVE BEHAVIOR

## 16.1. Desktop to tablet

* Grid bốn card chuyển thành hai card mỗi hàng.
* Hero text giảm kích thước trước khi thay đổi cấu trúc.
* Navigation chuyển sang mobile khi không còn đủ không gian.
* Không thu nhỏ nội dung đến mức khó đọc để giữ layout desktop.

## 16.2. Tablet to mobile

* Hai cột chuyển thành một cột.
* Section padding giảm từ 80–96px xuống 48–64px.
* Card padding có thể giảm từ 24px xuống 20px.
* Primary action có thể full-width.
* Table phức tạp chuyển thành scroll ngang hoặc card list.
* Hero content căn trái theo mặc định.

## 16.3. Mobile rules

* Không sử dụng body text nhỏ hơn 14px.
* Input nên dùng ít nhất 16px để tránh browser zoom.
* Không đặt ba button trên cùng một hàng.
* Không đặt text trực tiếp trên ảnh nếu ảnh không đảm bảo readability.
* Nội dung quan trọng phải xuất hiện trước nội dung trang trí.

---

# 17. CONTENT AND MICROCOPY

## 17.1. Voice

Giọng văn phải:

* Rõ ràng
* Tích cực
* Có uy tín
* Trực tiếp
* Không phô trương
* Không quá hành chính

## 17.2. Button labels

Tốt:

* Xem chương trình
* Khám phá EIU
* Đăng ký tư vấn
* Xem chi tiết
* Lưu thay đổi
* Tải tài liệu

Không tốt:

* Bấm vào đây
* Click here
* Submit
* OK
* Xem thêm thông tin chi tiết ngay bây giờ

## 17.3. Headings

* Heading phải thể hiện giá trị hoặc chủ đề rõ ràng.
* Tránh heading chung chung như “Thông tin”, “Nội dung”, “Khác”.
* Không thêm dấu chấm cuối heading.
* Hạn chế dấu chấm than.

---

# 18. COMPONENT DECISION MATRIX

AI hoặc developer phải dùng bảng sau trước khi tạo component mới:

| Nhu cầu                      | Component nên dùng             |
| ---------------------------- | ------------------------------ |
| Hành động chính              | Primary button                 |
| Hành động phụ                | Secondary hoặc tertiary button |
| Điều hướng đến nội dung khác | Text link hoặc clickable card  |
| Hiển thị nội dung cùng loại  | Card grid                      |
| Hiển thị trạng thái ngắn     | Badge                          |
| Thông báo trong luồng        | Inline alert                   |
| Thông báo toàn hệ thống      | Banner                         |
| Chọn một giá trị             | Radio hoặc select              |
| Chọn nhiều giá trị           | Checkbox                       |
| Hành động cần xác nhận       | Modal                          |
| Thông tin bổ sung nhỏ        | Tooltip                        |
| Dữ liệu nhiều hàng           | Table                          |
| Dữ liệu ít và cần responsive | List hoặc data card            |
| Nhóm nội dung dài            | Section                        |
| Quảng bá nội dung quan trọng | Hero hoặc feature panel        |

Không tạo component mới chỉ vì muốn giao diện “khác biệt”.

---

# 19. DESIGN CONSISTENCY RULES FOR AI

Khi AI tạo hoặc chỉnh sửa giao diện, bắt buộc thực hiện theo thứ tự sau:

## 19.1. Trước khi thiết kế

1. Xác định loại màn hình: marketing, content, form, dashboard hoặc detail.
2. Xác định hành động chính duy nhất.
3. Xác định hierarchy của nội dung.
4. Chọn component có sẵn.
5. Áp dụng token thay vì hard-code giá trị mới.

## 19.2. Trong khi thiết kế

AI phải:

* Dùng màu từ token system.
* Dùng spacing thuộc 4px scale.
* Dùng radius 4px hoặc 8px trong phần lớn trường hợp.
* Dùng một primary CTA trên mỗi khu vực.
* Giữ heading hierarchy đúng.
* Dùng neutral surface để phân section thay vì thêm nhiều border.
* Duy trì cùng card anatomy trong một danh sách.
* Giữ text alignment nhất quán.
* Kiểm tra mobile layout.
* Thêm hover, focus, disabled và loading state.

## 19.3. AI không được tự ý

* Tạo gradient mới.
* Tạo màu thương hiệu mới.
* Sử dụng neon color.
* Dùng glassmorphism.
* Dùng border radius quá lớn.
* Dùng shadow nặng cho mọi component.
* Tạo nhiều loại button có cùng mức ưu tiên.
* Trộn nhiều icon library.
* Dùng font mới.
* Thay đổi base spacing.
* Căn giữa đoạn văn dài.
* Đặt text body lên ảnh mà không có overlay.
* Dùng animation chỉ để trang trí.

---

# 20. DESIGN TOKENS

```css
:root {
  /* Brand colors */
  --color-brand-900: #003B71;
  --color-brand-800: #00518C;
  --color-brand-700: #0066A4;
  --color-brand-100: #EAF4FA;

  --color-accent-600: #D99A1A;
  --color-accent-500: #F2B233;

  /* Neutral colors */
  --color-white: #FFFFFF;
  --color-neutral-50: #F7F8FA;
  --color-neutral-100: #EEF1F4;
  --color-neutral-200: #E2E7EB;
  --color-neutral-300: #D3D9DF;
  --color-neutral-500: #84909A;
  --color-neutral-600: #66717D;
  --color-neutral-800: #263238;
  --color-neutral-900: #111820;

  /* Semantic colors */
  --color-success-600: #2E7D32;
  --color-success-100: #E8F5E9;
  --color-warning-600: #B7791F;
  --color-warning-100: #FFF8E1;
  --color-danger-600: #C62828;
  --color-danger-100: #FDECEC;
  --color-info-600: #0277BD;
  --color-info-100: #E6F4FB;

  /* Typography */
  --font-heading: "Montserrat", "Arial", sans-serif;
  --font-body: "Inter", "Arial", sans-serif;

  --text-xs: 12px;
  --text-sm: 14px;
  --text-base: 16px;
  --text-lg: 18px;
  --text-xl: 22px;
  --text-2xl: 28px;
  --text-3xl: 36px;
  --text-4xl: 44px;
  --text-display: 56px;

  /* Spacing */
  --space-1: 4px;
  --space-2: 8px;
  --space-3: 12px;
  --space-4: 16px;
  --space-5: 20px;
  --space-6: 24px;
  --space-7: 32px;
  --space-8: 40px;
  --space-9: 48px;
  --space-10: 64px;
  --space-11: 80px;
  --space-12: 96px;
  --space-13: 120px;

  /* Radius */
  --radius-xs: 2px;
  --radius-sm: 4px;
  --radius-md: 8px;
  --radius-lg: 12px;
  --radius-xl: 16px;
  --radius-full: 999px;

  /* Shadow */
  --shadow-xs: 0 1px 2px rgba(13, 42, 65, 0.06);
  --shadow-sm: 0 4px 12px rgba(13, 42, 65, 0.08);
  --shadow-md: 0 8px 24px rgba(13, 42, 65, 0.10);
  --shadow-lg: 0 16px 40px rgba(13, 42, 65, 0.14);

  /* Layout */
  --container-max: 1280px;
  --content-readable: 720px;

  /* Motion */
  --duration-fast: 150ms;
  --duration-normal: 200ms;
  --duration-slow: 300ms;
  --ease-standard: cubic-bezier(0.2, 0, 0, 1);
}
```

---

# 21. UI QUALITY CHECKLIST

Trước khi hoàn thành một màn hình, AI hoặc developer phải kiểm tra:

## Visual consistency

* [ ] Chỉ sử dụng màu trong design token.
* [ ] Không có spacing ngoài hệ thống nếu không có lý do rõ ràng.
* [ ] Radius nhất quán giữa các component cùng loại.
* [ ] Icon cùng style và cùng library.
* [ ] Heading hierarchy hợp lý.
* [ ] Card trong cùng grid có cùng cấu trúc.
* [ ] Chỉ có một hành động chính trong mỗi khu vực.

## Interaction

* [ ] Có hover state.
* [ ] Có keyboard focus state.
* [ ] Có disabled state.
* [ ] Có loading state cho hành động bất đồng bộ.
* [ ] Clickable area đủ lớn.
* [ ] Không phụ thuộc hoàn toàn vào màu sắc.

## Responsive

* [ ] Hoạt động ở 375px.
* [ ] Hoạt động ở 768px.
* [ ] Hoạt động ở 1024px.
* [ ] Hoạt động ở 1440px.
* [ ] Không có horizontal overflow ngoài khu vực chủ ý.
* [ ] Typography không quá nhỏ.
* [ ] Button không bị xuống dòng bất hợp lý.

## Content

* [ ] Button label dùng động từ.
* [ ] Heading mô tả đúng nội dung.
* [ ] Paragraph không quá dài.
* [ ] Empty state có hướng dẫn tiếp theo.
* [ ] Error message giúp người dùng sửa lỗi.

## Accessibility

* [ ] Contrast đạt yêu cầu.
* [ ] Form có label.
* [ ] Image có alt phù hợp.
* [ ] Component dùng được bằng keyboard.
* [ ] Focus indicator nhìn thấy rõ.
* [ ] Heading theo đúng thứ tự.

---

# 22. FINAL DESIGN DIRECTIVE

Tất cả giao diện mới phải tạo cảm giác cùng thuộc một hệ thống:

* Nền sáng và thông thoáng.
* Xanh đậm thể hiện uy tín.
* Vàng tạo điểm nhấn có kiểm soát.
* Typography sans-serif rõ ràng.
* Nội dung được phân cấp mạnh.
* Ảnh thật tạo cảm xúc.
* Component đơn giản, có cấu trúc.
* Hiệu ứng nhẹ và có mục đích.
* Không chạy theo xu hướng thị giác nếu xu hướng đó làm giảm tính học thuật, khả năng sử dụng hoặc tính nhất quán.

Khi phải lựa chọn giữa một thiết kế sáng tạo nhưng khác hệ thống và một thiết kế đơn giản nhưng nhất quán, luôn chọn phương án nhất quán.
