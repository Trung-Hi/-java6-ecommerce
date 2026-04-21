const toQuery = (params = {}) => {
    const search = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
        if (value === undefined || value === null || value === "") {
            return;
        }
        search.append(key, String(value));
    });
    const text = search.toString();
    return text ? `?${text}` : "";
};

const request = async (path, options = {}) => {
    const response = await fetch(path, {
        credentials: "include",
        headers: {
            ...(options.body instanceof FormData ? {} : {"Content-Type": "application/json"}),
            ...(options.headers || {})
        },
        ...options
    });
    let payload = null;
    try {
        payload = await response.json();
    } catch (e) {
        payload = {success: false, message: "Không đọc được phản hồi từ server", data: null};
    }
    if (!response.ok) {
        const message = payload?.message || `HTTP ${response.status}`;
        throw new Error(message);
    }
    return payload;
};

const json = (path, method, body) => request(path, {method, body: JSON.stringify(body)});
const form = (path, method, data) => {
    const formData = new FormData();
    Object.entries(data).forEach(([key, value]) => {
        if (value === undefined || value === null || value === "") {
            return;
        }
        formData.append(key, value);
    });
    return request(path, {method, body: formData});
};

export const api = {
    auth: {
        login: (username, password) => json("/api/auth/login", "POST", {username, password}),
        me: () => request("/api/auth/me"),
        logout: () => json("/api/auth/logout", "POST", {})
    },
    home: {
        index: (params) => request(`/api/home/index${toQuery(params)}`)
    },
    products: {
        list: (params) => request(`/api/store/products${toQuery(params)}`),
        listByCategory: (id, params) => request(`/api/store/products/category/${id}${toQuery(params)}`),
        detail: (id) => request(`/api/store/products/${id}`)
    },
    cart: {
        get: () => request("/api/cart"),
        add: (productId, sizeId) => request(`/api/cart/items/${productId}${toQuery({sizeId})}`, {method: "POST"}),
        addDetail: (productId, sizeId, quantity) => request(`/api/cart/items${toQuery({productId, sizeId, quantity})}`, {method: "POST"}),
        update: (productId, sizeId, quantity) => request(`/api/cart/items${toQuery({productId, sizeId, quantity})}`, {method: "PUT"}),
        remove: (productId, sizeId) => request(`/api/cart/items/${productId}${toQuery({sizeId})}`, {method: "DELETE"}),
        clear: () => request("/api/cart/clear", {method: "DELETE"})
    },
    account: {
        signUp: (data) => form("/api/account/sign-up", "POST", data),
        profile: () => request("/api/account/profile"),
        updateProfile: (data) => form("/api/account/profile", "POST", data),
        changePassword: (currentPassword, newPassword) => form("/api/account/change-password", "POST", {currentPassword, newPassword}),
        forgotPassword: (email) => form("/api/account/forgot-password", "POST", {email})
    },
    orderWorkflow: {
        checkoutData: () => request("/api/order-workflow/checkout"),
        checkout: (data) => form("/api/order-workflow/checkout", "POST", data),
        bankTransfer: (id) => request(`/api/order-workflow/bank-transfer/${id}`),
        confirmBankTransfer: (orderId) => form("/api/order-workflow/bank-transfer/confirm", "POST", {orderId}),
        switchToCod: (orderId) => form("/api/order-workflow/bank-transfer/cancel/switch-cod", "POST", {orderId}),
        cancelAndDelete: (orderId) => form("/api/order-workflow/bank-transfer/cancel/delete", "POST", {orderId}),
        orderList: () => request("/api/order-workflow/list"),
        orderDetail: (id) => request(`/api/order-workflow/detail/${id}`),
        myProducts: () => request("/api/order-workflow/my-product-list"),
        payosStatus: (orderId) => request(`/api/order-workflow/payos/status${toQuery({orderId})}`)
    },
    admin: {
        accounts: {
            list: () => request("/api/admin/accounts"),
            detail: (username) => request(`/api/admin/accounts/${username}`),
            create: (data) => form("/api/admin/accounts", "POST", data),
            update: (username, data) => form(`/api/admin/accounts/${username}`, "PUT", data),
            remove: (username) => request(`/api/admin/accounts/${username}`, {method: "DELETE"})
        },
        categories: {
            list: () => request("/api/admin/categories"),
            detail: (id) => request(`/api/admin/categories/${id}`),
            create: (id, name) => request(`/api/admin/categories${toQuery({id, name})}`, {method: "POST"}),
            update: (id, name) => request(`/api/admin/categories/${id}${toQuery({name})}`, {method: "PUT"}),
            remove: (id) => request(`/api/admin/categories/${id}`, {method: "DELETE"})
        },
        products: {
            list: (params) => request(`/api/admin/products${toQuery(params)}`),
            detail: (id, params) => request(`/api/admin/products/${id}${toQuery(params)}`),
            create: (data) => form("/api/admin/products", "POST", data),
            update: (id, data) => form(`/api/admin/products/${id}`, "PUT", data),
            remove: (id) => request(`/api/admin/products/${id}`, {method: "DELETE"})
        },
        orders: {
            list: () => request("/api/admin/orders"),
            detail: (id) => request(`/api/admin/orders/${id}`),
            updateStatus: (id, status) => request(`/api/admin/orders/${id}/status${toQuery({status})}`, {method: "PUT"}),
            remove: (id) => request(`/api/admin/orders/${id}`, {method: "DELETE"}),
            cancelPayos: (orderCode) => request(`/api/admin/orders/payos/cancel${toQuery({orderCode})}`, {method: "POST"})
        },
        reports: {
            revenue: (params) => request(`/api/admin/reports/revenue${toQuery(params)}`),
            vip: () => request("/api/admin/reports/vip")
        },
        camera: {
            info: () => request("/api/admin/camera")
        }
    },
    notifications: {
        read: (id) => request(`/api/notifications/read/${id}`)
    }
};
