const MOJIBAKE_PATTERN = /(?:Ã[\u0080-\u024F]|Â[\u0080-\u024F]|Ä[\u0080-\u024F]|Å[\u0080-\u024F]|Æ[\u0080-\u024F]|Ð[\u0080-\u024F]|ð[\u0080-\u024F]|á[\u0080-\u024F]|à[\u0080-\u024F]|â[\u0080-\u024F]|ã[\u0080-\u024F]|ï¿½|�|[\u0080-\u009F])/;
const MOJIBAKE_CHUNK_PATTERN = /(?:Ã[\u0080-\u024F]|Â[\u0080-\u024F]|Ä[\u0080-\u024F]|Å[\u0080-\u024F]|Æ[\u0080-\u024F]|Ð[\u0080-\u024F]|ð[\u0080-\u024F]|á[\u0080-\u024F]|à[\u0080-\u024F]|â[\u0080-\u024F]|ã[\u0080-\u024F]|ï¿½|�|[\u0080-\u009F])/g;
const VIETNAMESE_CHAR_PATTERN = /[À-ỹĐđ]/g;
const MAX_DECODE_PASSES = 4;
const CP1252_REVERSE_MAP = new Map([
  [0x20ac, 0x80], [0x201a, 0x82], [0x0192, 0x83], [0x201e, 0x84],
  [0x2026, 0x85], [0x2020, 0x86], [0x2021, 0x87], [0x02c6, 0x88],
  [0x2030, 0x89], [0x0160, 0x8a], [0x2039, 0x8b], [0x0152, 0x8c],
  [0x017d, 0x8e], [0x2018, 0x91], [0x2019, 0x92], [0x201c, 0x93],
  [0x201d, 0x94], [0x2022, 0x95], [0x2013, 0x96], [0x2014, 0x97],
  [0x02dc, 0x98], [0x2122, 0x99], [0x0161, 0x9a], [0x203a, 0x9b],
  [0x0153, 0x9c], [0x017e, 0x9e], [0x0178, 0x9f],
]);
const VIETNAMESE_PHRASE_REPLACEMENTS = [
  ['Trang chu', 'Trang chủ'],
  ['Danh sach khoa hoc', 'Danh sách khóa học'],
  ['Tin tuc va khuyen mai', 'Tin tức và khuyến mãi'],
  ['Bai viet', 'Bài viết'],
  ['Dang nhap', 'Đăng nhập'],
  ['Dang ky', 'Đăng ký'],
  ['Quen mat khau', 'Quên mật khẩu'],
  ['Ho so', 'Hồ sơ'],
  ['Khoa hoc cua toi', 'Khóa học của tôi'],
  ['Yeu thich', 'Yêu thích'],
  ['Tin nhan', 'Tin nhắn'],
  ['Gio hang', 'Giỏ hàng'],
  ['Thanh toan', 'Thanh toán'],
  ['Ket qua thanh toan', 'Kết quả thanh toán'],
  ['Lich su giao dich', 'Lịch sử giao dịch'],
  ['Thanh tich', 'Thành tích'],
  ['Bo suu tap chung chi', 'Bộ sưu tập chứng chỉ'],
  ['Chi tiet khoa hoc', 'Chi tiết khóa học'],
  ['Ho so giang vien', 'Hồ sơ giảng viên'],
  ['Goc hoc tap', 'Góc học tập'],
  ['Tro thanh giang vien', 'Trở thành giảng viên'],
  ['Quan ly noi dung', 'Quản lý nội dung'],
  ['Hoi dap', 'Hỏi đáp'],
  ['Bai tap', 'Bài tập'],
  ['Thong bao', 'Thông báo'],
  ['Tai nguyen', 'Tài nguyên'],
  ['Cong cu', 'Công cụ'],
  ['Rut tien', 'Rút tiền'],
  ['Xem truoc bai hoc', 'Xem trước bài học'],
  ['Yeu cau dang nhap', 'Yêu cầu đăng nhập'],
  ['Di den dang nhap', 'Đi đến đăng nhập'],
  ['O lai', 'Ở lại'],
  ['Ban can dang nhap de tiep tuc. Chuyen toi trang dang nhap?', 'Bạn cần đăng nhập để tiếp tục. Chuyển tới trang đăng nhập?'],
  ['Ban khong co quyen truy cap trang nay.', 'Bạn không có quyền truy cập trang này.'],
  ['Ban khong co quyen truy cap chuc nang nay.', 'Bạn không có quyền truy cập chức năng này.'],
  ['Chi Super Admin moi co quyen truy cap trang nay.', 'Chỉ Super Admin mới có quyền truy cập trang này.'],
  ['Khong co ma giam gia', 'Không có mã giảm giá'],
  ['Ap dung ma giam gia thanh cong', 'Áp dụng mã giảm giá thành công'],
  ['Ma giam gia khong ap dung cho khoa hoc dang flash sale hoac gia qua thap.', 'Mã giảm giá không áp dụng cho khóa học đang flash sale hoặc giá quá thấp.'],
  ['Ban da so huu khoa hoc', 'Bạn đã sở hữu khóa học'],
  ['Khong tim thay khoa hoc', 'Không tìm thấy khóa học'],
  ['Gio hang trong', 'Giỏ hàng trống'],
  ['Vui long chon khoa hoc', 'Vui lòng chọn khóa học'],
  ['Khong co khoa hoc hop le de thanh toan.', 'Không có khóa học hợp lệ để thanh toán.'],
  ['Khong xac dinh duoc nguoi dung hien tai.', 'Không xác định được người dùng hiện tại.'],
  ['Ban khong co quyen tai bien nhan cua don hang nay.', 'Bạn không có quyền tải biên nhận của đơn hàng này.'],
  ['Ban khong co quyen truy cap du lieu don hang cua tai khoan nay.', 'Bạn không có quyền truy cập dữ liệu đơn hàng của tài khoản này.'],
  ['Khoa hoc hien khong mo ban.', 'Khóa học hiện không mở bán.'],
  ['Giang vien cua khoa hoc hien dang bi khoa. Tam thoi khong the mua moi.', 'Giảng viên của khóa học hiện đang bị khóa. Tạm thời không thể mua mới.'],
  ['Ban khong the mua khoa hoc cua chinh minh.', 'Bạn không thể mua khóa học của chính mình.'],
  ['Thanh toan thanh cong', 'Thanh toán thành công'],
  ['Co hoc vien moi dang ky', 'Có học viên mới đăng ký'],
  ['Khoa hoc da bi xoa', 'Khóa học đã bị xóa'],
  ['Khoa hoc bi tam khoa', 'Khóa học bị tạm khóa'],
  ['Khoa hoc da duoc duyet', 'Khóa học đã được duyệt'],
  ['Khoa hoc bi tu choi', 'Khóa học bị từ chối'],
  ['Cap nhat trang thai khoa hoc', 'Cập nhật trạng thái khóa học']
];

function toSingleByteCode(charCode) {
  if (charCode <= 0xff) return charCode;
  if (CP1252_REVERSE_MAP.has(charCode)) return CP1252_REVERSE_MAP.get(charCode);
  return 0x3f;
}

function decodeLatin1AsUtf8(text) {
  const bytes = new Uint8Array(Array.from(text, (ch) => toSingleByteCode(ch.charCodeAt(0))));
  return new TextDecoder('utf-8', { fatal: false }).decode(bytes);
}

function buildDecodeCandidates(value) {
  const candidates = [value];
  let current = value;

  for (let pass = 0; pass < MAX_DECODE_PASSES; pass += 1) {
    const decoded = decodeLatin1AsUtf8(current);
    if (!decoded || decoded === current) break;
    candidates.push(decoded);
    current = decoded;
    if (!MOJIBAKE_PATTERN.test(decoded)) break;
  }

  return candidates;
}

function questionMarkCount(text) {
  if (typeof text !== 'string' || !text) return 0;
  return (text.match(/\?/g) || []).length;
}

function decodeSegmentSafely(segment) {
  if (typeof segment !== 'string' || !segment || !MOJIBAKE_PATTERN.test(segment)) {
    return segment;
  }

  const decoded = buildDecodeCandidates(segment).reduce((best, current) => {
    const bestScore = mojibakeScore(best);
    const currentScore = mojibakeScore(current);
    if (currentScore < bestScore) return current;
    if (currentScore === bestScore && vietnameseScore(current) > vietnameseScore(best)) return current;
    return best;
  }, segment);

  if (
    questionMarkCount(decoded) > questionMarkCount(segment) + 1
    && vietnameseScore(decoded) <= vietnameseScore(segment)
  ) {
    return segment;
  }

  return decoded;
}

function decodeBySegments(value) {
  if (typeof value !== 'string' || !value) return value;
  return value
    .split(/(\s+)/)
    .map((part) => decodeSegmentSafely(part))
    .join('');
}

function mojibakeScore(text) {
  if (typeof text !== 'string' || !text) return 0;
  const replacement =
    (text.match(/ï¿½/g) || []).length * 8 +
    (text.match(/ï¿½/g) || []).length * 8;
  const artifacts = (text.match(MOJIBAKE_CHUNK_PATTERN) || []).length;
  const questionPenalty = Math.max(0, questionMarkCount(text) - 1) * 4;
  return replacement + artifacts + questionPenalty;
}

function vietnameseScore(text) {
  if (typeof text !== 'string' || !text) return 0;
  return (text.match(VIETNAMESE_CHAR_PATTERN) || []).length;
}

export function normalizeMojibakeText(value) {
  if (typeof value !== 'string' || !value) return value;
  const source = normalizeVietnamesePhrases(value);
  if (!MOJIBAKE_PATTERN.test(source)) return source;

  const globalBest = buildDecodeCandidates(source).reduce((best, current) => {
    const bestScore = mojibakeScore(best);
    const currentScore = mojibakeScore(current);
    if (currentScore < bestScore) return current;
    if (currentScore === bestScore && vietnameseScore(current) > vietnameseScore(best)) {
      return current;
    }
    return best;
  }, source);

  const segmentedBest = decodeBySegments(source);

  const globalScore = mojibakeScore(globalBest);
  const segmentedScore = mojibakeScore(segmentedBest);
  if (segmentedScore < globalScore) return segmentedBest;
  if (segmentedScore === globalScore && vietnameseScore(segmentedBest) > vietnameseScore(globalBest)) {
    return segmentedBest;
  }
  return normalizeVietnamesePhrases(globalBest);
}

export function normalizeMojibakeDeep(payload) {
  if (payload == null) return payload;

  if (typeof payload === 'string') {
    return normalizeMojibakeText(payload);
  }

  const isBlob = typeof Blob !== 'undefined' && payload instanceof Blob;
  const isFile = typeof File !== 'undefined' && payload instanceof File;
  const isFormData = typeof FormData !== 'undefined' && payload instanceof FormData;
  const isSearchParams = typeof URLSearchParams !== 'undefined' && payload instanceof URLSearchParams;

  if (isBlob || isFile || payload instanceof Date || isFormData || isSearchParams || payload instanceof ArrayBuffer) {
    return payload;
  }

  if (Array.isArray(payload)) {
    return payload.map(normalizeMojibakeDeep);
  }

  if (typeof payload === 'object') {
    const output = {};
    Object.entries(payload).forEach(([key, value]) => {
      output[key] = normalizeMojibakeDeep(value);
    });
    return output;
  }

  return payload;
}

function normalizeVietnamesePhrases(value) {
  if (typeof value !== 'string' || !value) return value;

  return VIETNAMESE_PHRASE_REPLACEMENTS.reduce((output, [plain, accented]) => {
    return output.replaceAll(plain, accented);
  }, value);
}
