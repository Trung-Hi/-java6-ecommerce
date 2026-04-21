export const normalizePhone = (value) => String(value || "").replace(/\s+/g, "").trim();

export const isValidVnPhone10 = (value) => {
    const phone = normalizePhone(value);
    if (!/^0\d{9}$/.test(phone)) {
        return false;
    }
    return phone !== "0000000000";
};
