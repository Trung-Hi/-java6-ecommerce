<script setup>
import {computed, ref, onMounted, onUnmounted} from "vue";
import Swal from "sweetalert2";
import "sweetalert2/dist/sweetalert2.min.css";
import {AdminAccountPage} from "@/legacy/pages";
import {useSession} from "@/composables/useSession";
import AdminLayout from "@/components/AdminLayout.vue";
import {api} from "@/api";

const {rows, roles, form, modalOpen, editing, msg, load, openCreate, closeModal: originalCloseModal, onPhotoChange, save: originalSave, remove} = AdminAccountPage.setup();
const {state} = useSession();
const currentUsername = computed(() => state.me?.username || "");
const searchQuery = ref("");

const visibleRows = computed(() => {
    let filtered = (rows.value || []).filter((u) => {
        if (u?.username === currentUsername.value) return false;
        if (u?.isDelete === true || u?.is_delete === true) return false;
        return true;
    });
    
    if (searchQuery.value.trim()) {
        const query = searchQuery.value.toLowerCase();
        filtered = filtered.filter(u => 
            u?.username?.toLowerCase().includes(query) ||
            u?.fullname?.toLowerCase().includes(query) ||
            u?.email?.toLowerCase().includes(query) ||
            u?.phone?.toLowerCase().includes(query)
        );
    }
    
    return filtered;
});

const hasFilters = computed(() => searchQuery.value.trim());
const previewOpen = ref(false);
const previewSrc = ref("");
const previewName = ref("");
const previewImageError = ref(false);
const updateNoticeOpen = ref(false);
const updateNoticeText = ref("");

const roleOptions = computed(() => (roles.value || []).map((r) => r.id));
const roleLabel = (roleId) => {
    const key = String(roleId || "").toUpperCase();
    if (key === "ADMIN") return "Quản trị viên";
    if (key === "USER") return "Khách hàng";
    return key || "Khách hàng";
};

// Avatar component logic
const getAvatarInitials = (name) => {
    const text = String(name || "").trim();
    if (!text) return "UN";
    const words = text.split(" ").filter(w => w);
    if (words.length >= 2) {
        return (words[0][0] + words[words.length - 1][0]).toUpperCase();
    }
    return text.slice(0, 2).toUpperCase();
};

const getAvatarColor = (name) => {
    const colors = [
        "#FFE4E6", "#FCE7F3", "#FBCFE8", "#F9A8D4",
        "#E0E7FF", "#C7D2FE", "#A5B4FC", "#818CF8",
        "#D1FAE5", "#A7F3D0", "#6EE7B7", "#34D399",
        "#FEF3C7", "#FDE68A", "#FCD34D", "#FBBF24",
        "#E0F2FE", "#BAE6FD", "#7DD3FC", "#38BDF8"
    ];
    let hash = 0;
    const text = String(name || "");
    for (let i = 0; i < text.length; i++) {
        hash = text.charCodeAt(i) + ((hash << 5) - hash);
    }
    return colors[Math.abs(hash) % colors.length];
};

const getAvatarTextColor = (bgColor) => {
    // Return darker text for light backgrounds
    return "#1e293b";
};

const resolveAvatarImage = (photo) => {
    const raw = String(photo || "").trim();
    return raw
        ? (/^https?:\/\//i.test(raw) ? raw : (raw.startsWith("/") ? raw : `/images/${raw}`))
        : null;
};

// Smart address logic
const getShortAddress = (address) => {
    const text = String(address || "").trim();
    if (!text) return "-";
    
    // Split by comma and get the last part (usually city/province)
    const parts = text.split(",").map(p => p.trim()).filter(p => p);
    if (parts.length <= 1) return text;
    return parts[parts.length - 1];
};

const openPreview = (u) => {
    const raw = String(u?.photo || "").trim();
    previewName.value = u?.username || "";
    previewSrc.value = raw
        ? (/^https?:\/\//i.test(raw) ? raw : (raw.startsWith("/") ? raw : `/images/${raw}`))
        : "/images/logo.png";
    previewImageError.value = false;
    previewOpen.value = true;
};
const closePreview = () => {
    previewOpen.value = false;
    previewSrc.value = "";
    previewName.value = "";
    previewImageError.value = false;
};
const previewInitial = computed(() => {
    const text = String(previewName.value || "").trim();
    return text ? text.slice(0, 1).toUpperCase() : "U";
});
const openUpdateNotice = (text) => {
    updateNoticeText.value = text || "Cập nhật thành công.";
    updateNoticeOpen.value = true;
};
const closeUpdateNotice = () => {
    updateNoticeOpen.value = false;
    updateNoticeText.value = "";
};
const updateRole = async (u, event) => {
    const nextRole = String(event?.target?.value || "").trim();
    if (!u?.username || !nextRole) return;
    try {
        await api.admin.accounts.updateRole(u.username, nextRole);
        await load();
        openUpdateNotice(`Đã cập nhật vai trò của tài khoản ${u.username}.`);
    } catch (e) {
        event.target.value = u.roleId || "USER";
    }
};
const updateActivation = async (u, event) => {
    if (!u?.username) return;
    const value = String(event?.target?.value || "active");
    const activated = value === "active";
    try {
        await api.admin.accounts.updateActivation(u.username, activated);
        await load();
        openUpdateNotice(`Đã cập nhật trạng thái tài khoản ${u.username}.`);
    } catch (e) {
        event.target.value = u.activated ? "active" : "locked";
    }
};

const handleDelete = async (u) => {
    if (!u?.username) return;
    
    const result = await Swal.fire({
        title: "Bạn có chắc chắn muốn xóa?",
        html: `Tài khoản <strong>"${u.username}"</strong> sẽ bị vô hiệu hóa. Dữ liệu lịch sử vẫn được giữ lại.`,
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Xác nhận xóa",
        cancelButtonText: "Hủy",
        confirmButtonColor: "#ef4444",
        cancelButtonColor: "#6b7280",
        reverseButtons: true,
        focusCancel: true
    });
    
    if (result.isConfirmed) {
        Swal.fire({
            title: "Đang xử lý...",
            showConfirmButton: false,
            didOpen: () => Swal.showLoading()
        });
        
        await remove(u.username);
        
        Swal.fire({
            title: "Đã xóa!",
            text: `Tài khoản đã được vô hiệu hóa.`,
            icon: "success",
            timer: 3000,
            timerProgressBar: true
        });
        
        await load();
    }
};

const handleEdit = async (u) => {
    if (!u?.username) return;
    // Fetch user data from API
    try {
        const res = await api.admin.accounts.detail(u.username);
        const data = res.data || {};
        form.username = data.account?.username || u.username;
        form.fullname = data.account?.fullname || "";
        form.email = data.account?.email || "";
        form.phone = data.account?.phone || "";
        form.address = data.account?.address || "";
        form.photo = data.account?.photo || "";
        form.photoFile = null;
        form.password = "";
        form.activated = data.account?.activated ?? true;
        form.roleId = data.roleId || "USER";
        editing.value = true;
        modalOpen.value = true;
    } catch (e) {
        console.error("Failed to load user data:", e);
    }
};

const handleResetPassword = async (u) => {
    if (!u?.username) return;
    
    const result = await Swal.fire({
        title: "Đặt lại mật khẩu?",
        html: `Mật khẩu của <strong>"${u.username}"</strong> sẽ được đặt lại về mật khẩu mặc định.`,
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Đặt lại",
        cancelButtonText: "Hủy",
        confirmButtonColor: "#f59e0b",
        cancelButtonColor: "#6b7280",
        reverseButtons: true,
        focusCancel: true
    });
    
    if (result.isConfirmed) {
        try {
            Swal.fire({
                title: "Đang xử lý...",
                showConfirmButton: false,
                didOpen: () => Swal.showLoading()
            });
            
            const res = await api.admin.accounts.resetPassword(u.username);
            const newPass = res.data?.tempPassword || res.data?.newPassword;
            
            Swal.fire({
                title: "Đã đặt lại!",
                html: `Mật khẩu mới đã được gửi đến email của người dùng.<br><br>Mật khẩu: <strong>${newPass || "(Không lấy được)"}</strong>`,
                icon: "success",
                timer: 5000,
                timerProgressBar: true
            });
        } catch (e) {
            Swal.fire({
                title: "Lỗi!",
                text: e.message || "Không thể đặt lại mật khẩu",
                icon: "error"
            });
        }
    }
};

// Modal fixes - prevent closing on outside click or Esc
const handleEscKey = (e) => {
    if (e.key === 'Escape' && modalOpen.value) {
        e.preventDefault();
        e.stopPropagation();
    }
};

const enhancedCloseModal = () => {
    document.removeEventListener('keydown', handleEscKey);
    originalCloseModal();
};

const enhancedOpenCreate = () => {
    document.addEventListener('keydown', handleEscKey);
    // Reset form before opening for create
    form.username = "";
    form.password = "";
    form.fullname = "";
    form.email = "";
    form.phone = "";
    form.address = "";
    form.roleId = "USER";
    form.activated = true;
    form.photo = "";
    form.photoFile = null;
    editing.value = false;
    modalOpen.value = true;
};

// Load data on mount
onMounted(() => {
    load();
});

// Override save with SweetAlert2 error handling
const save = async () => {
    try {
        // Validation với SweetAlert
        if (!form.phone?.trim()) {
            await Swal.fire({
                title: "Lỗi!",
                text: "Số điện thoại là bắt buộc",
                icon: "error",
                confirmButtonColor: "#ef4444",
                customClass: { popup: "swal-high-zindex" }
            });
            return;
        }
        
        // Loading
        Swal.fire({
            title: "Đang xử lý...",
            allowOutsideClick: false,
            customClass: { popup: "swal-high-zindex" },
            didOpen: () => Swal.showLoading()
        });
        
        // Call API
        await originalSave();
        
        // Success
        Swal.close();
        await Swal.fire({
            title: editing.value ? "Cập nhật thành công!" : "Tạo tài khoản thành công!",
            icon: "success",
            customClass: { popup: "swal-high-zindex" }
        });
        
        await load();
        enhancedCloseModal();
    } catch (error) {
        Swal.close();
        
        let errorMessage = "Có lỗi xảy ra. Vui lòng thử lại.";
        if (error?.response?.data?.message) {
            errorMessage = error.response.data.message;
        } else if (error?.message) {
            errorMessage = error.message;
        }
        
        await Swal.fire({
            title: "Lỗi!",
            html: `<div style="max-width: 400px;">${errorMessage}</div>`,
            icon: "error",
            customClass: { popup: "swal-high-zindex" }
        });
    }
};
</script>

<template>
    <AdminLayout>
        <div class="status-message" v-if="msg">{{ msg }}</div>
        <div class="admin-account-header">
            <div class="header-left">
                <h3 class="page-title">Danh sách tài khoản</h3>
                <span class="filter-results" v-if="hasFilters">Kết quả tìm kiếm: {{ searchQuery }}</span>
            </div>
            <div class="header-right">
                <button @click="enhancedOpenCreate" class="add-account-btn">
                    <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                        <line x1="12" y1="5" x2="12" y2="19"></line>
                        <line x1="5" y1="12" x2="19" y2="12"></line>
                    </svg>
                    Thêm tài khoản
                </button>
            </div>
        </div>
        
        <div class="table-card glassmorphism">
            <div class="filter-section">
                <div class="filterBar">
                    <div class="filterGroup filterSearch">
                        <svg class="filterIcon" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                            <circle cx="11" cy="11" r="8"></circle>
                            <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
                        </svg>
                        <input v-model="searchQuery" class="filterInput" placeholder="Tìm theo username, họ tên, email, SĐT...">
                    </div>
                </div>
            </div>
            
            <div class="table-wrapper" v-if="visibleRows.length">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Ảnh</th>
                            <th>Username</th>
                            <th>Họ tên</th>
                            <th>Email</th>
                            <th>Số điện thoại</th>
                            <th>Địa chỉ</th>
                            <th>Vai trò</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="u in visibleRows" :key="u.username" class="table-row-hover">
                            <td>
                                <div 
                                    class="avatar-circle"
                                    :style="resolveAvatarImage(u.photo) ? '' : { backgroundColor: getAvatarColor(u.fullname || u.username), color: getAvatarTextColor(getAvatarColor(u.fullname || u.username)) }"
                                    @click="openPreview(u)"
                                >
                                    <img 
                                        v-if="resolveAvatarImage(u.photo)" 
                                        :src="resolveAvatarImage(u.photo)" 
                                        :alt="u.username"
                                        class="avatar-img"
                                        @error="$el.style.display='none'; $el.nextElementSibling.style.display='flex'"
                                    >
                                    <span v-else class="avatar-initials">{{ getAvatarInitials(u.fullname || u.username) }}</span>
                                </div>
                            </td>
                            <td class="font-semibold">{{ u.username }}</td>
                            <td>{{ u.fullname }}</td>
                            <td>{{ u.email }}</td>
                            <td>{{ u.phone }}</td>
                            <td>
                                <span class="address-text" :title="u.address">{{ getShortAddress(u.address) }}</span>
                            </td>
                            <td>
                                <span class="badge badge-role" :class="String(u.roleId || 'USER').toUpperCase() === 'ADMIN' ? 'badge-admin' : 'badge-user'">
                                    {{ roleLabel(u.roleId) }}
                                </span>
                            </td>
                            <td>
                                <span class="badge badge-status" :class="u.activated ? 'badge-active' : 'badge-locked'">
                                    {{ u.activated ? "Mở khóa" : "Khóa" }}
                                </span>
                            </td>
                            <td>
                                <div class="actionButtons">
                                    <button class="actionBtn actionEditBtn" type="button" @click="handleEdit(u)" title="Chỉnh sửa">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                            <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                                            <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                                        </svg>
                                    </button>
                                    <button class="actionBtn actionResetBtn" type="button" @click="handleResetPassword(u)" title="Đặt lại mật khẩu">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                            <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                                            <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
                                        </svg>
                                    </button>
                                    <button class="actionBtn actionDeleteBtn" type="button" @click="handleDelete(u)" title="Xóa">
                                        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                            <polyline points="3 6 5 6 21 6"></polyline>
                                            <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                                        </svg>
                                    </button>
                                </div>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
            
            <div class="empty-state" v-else>
                <div class="empty-icon">
                    <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                        <circle cx="11" cy="11" r="8"/>
                        <path d="M21 21l-4.35-4.35"/>
                    </svg>
                </div>
                <h4 class="empty-title">Không có dữ liệu</h4>
                <p class="empty-text" v-if="hasFilters">Vui lòng thử điều chỉnh lại bộ lọc tìm kiếm</p>
            </div>
        </div>
        <div class="modal-backdrop userModalStatic" :class="{open: modalOpen}" v-if="modalOpen">
            <div class="admin-modal-panel">
                <form @submit.prevent="save">
                    <div class="modal-header">
                        <h4>{{ editing ? "Cập nhật tài khoản" : "Thêm tài khoản" }}</h4>
                        <button type="button" class="btn btn-outline-primary" @click="enhancedCloseModal">Đóng</button>
                    </div>
                    <div class="admin-form-grid">
                        <div class="form-group">
                            <label>Username</label>
                            <input type="text" v-model="form.username" :readonly="editing" required class="form-control">
                        </div>
                        <div class="form-group" v-if="!editing">
                            <label>Mật khẩu</label>
                            <input type="password" v-model="form.password" class="form-control" placeholder="Nhập mật khẩu" autocomplete="new-password" name="password" required>
                        </div>
                        <div class="form-group">
                            <label>Họ tên</label>
                            <input type="text" v-model="form.fullname" required class="form-control">
                        </div>
                        <div class="form-group">
                            <label>Email</label>
                            <input type="email" v-model="form.email" required class="form-control">
                        </div>
                        <div class="form-group">
                            <label>Số điện thoại</label>
                            <input type="text" v-model.trim="form.phone" required class="form-control" pattern="^(0|\+84)\d{9,10}$" placeholder="VD: 0912345678">
                        </div>
                        <div class="form-group">
                            <label>Địa chỉ</label>
                            <input type="text" v-model.trim="form.address" required class="form-control" placeholder="Nhập địa chỉ">
                        </div>
                        <div class="form-group">
                            <label>Vai trò</label>
                            <select v-model="form.roleId" class="form-control">
                                <option value="">Chọn vai trò</option>
                                <option v-for="r in roles" :key="r.id" :value="r.id">{{ r.id }}</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label>Kích hoạt</label>
                            <div class="checkbox-wrapper">
                                <input type="checkbox" v-model="form.activated" id="activated">
                                <label for="activated" class="checkbox-label">Tài khoản đang hoạt động</label>
                            </div>
                        </div>
                        <div class="form-group full-span">
                            <label>Ảnh đại diện</label>
                            <input type="file" accept="image/*" class="form-control" @change="onPhotoChange">
                            <div class="form-hint">Ảnh hiện tại: {{ form.photo || "Chưa có" }}</div>
                        </div>
                        <div class="admin-form-actions">
                            <button class="btn btn-primary" type="submit">{{ editing ? "Cập nhật" : "Thêm" }}</button>
                            <button class="btn btn-outline-primary" type="button" @click="enhancedCloseModal">Huỷ</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
        <div class="modal-backdrop" :class="{open: previewOpen}" v-if="previewOpen" @click.self="closePreview">
            <div class="admin-modal-panel" style="max-width: 360px; text-align: center;">
                <h4 style="margin: 0 0 12px 0;">Ảnh đại diện: {{ previewName }}</h4>
                <img
                    v-if="!previewImageError"
                    :src="previewSrc"
                    alt="avatar"
                    style="width:300px;height:300px;object-fit:cover;border-radius:12px;border:1px solid #e5e7eb;"
                    @error="previewImageError = true"
                >
                <div
                    v-else
                    style="width:300px;height:300px;border-radius:12px;border:1px solid #e5e7eb;background:#eef2ff;color:#3730a3;font-size:120px;font-weight:800;display:flex;align-items:center;justify-content:center;user-select:none;"
                >{{ previewInitial }}</div>
                <div style="margin-top: 12px;">
                    <button class="btn btn-outline-primary" type="button" @click="closePreview">Đóng</button>
                </div>
            </div>
        </div>
        <div class="modal-backdrop" :class="{open: updateNoticeOpen}" v-if="updateNoticeOpen" @click.self="closeUpdateNotice">
            <div class="admin-modal-panel" style="max-width: 420px; text-align: center;">
                <h4 style="margin: 0 0 8px 0;">Thông báo</h4>
                <p style="margin: 0; color: #374151;">{{ updateNoticeText }}</p>
                <div style="margin-top: 14px;">
                    <button class="btn btn-primary" type="button" @click="closeUpdateNotice">Đã hiểu</button>
                </div>
            </div>
        </div>
    </AdminLayout>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');

.userModalStatic {
    pointer-events: none;
}

.userModalStatic .admin-modal-panel {
    pointer-events: auto;
}

/* Apply Inter font to the entire component */
* {
    font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* ==================== TABLE STYLES ==================== */
.table-card {
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    border-radius: 16px;
    border: 1px solid rgba(226, 232, 240, 0.6);
    box-shadow: 0 4px 20px -4px rgba(0, 0, 0, 0.08);
    overflow: hidden;
}

.glassmorphism {
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
}

.filter-section {
    padding: 16px 20px;
    background: rgba(255, 255, 255, 0.5);
    border-bottom: 1px solid rgba(226, 232, 240, 0.6);
}

.filterBar {
    display: flex;
    align-items: center;
    gap: 12px;
}

.filterGroup {
    display: flex;
    align-items: center;
    position: relative;
}

.filterSearch {
    flex: 1;
    max-width: 400px;
}

.filterIcon {
    position: absolute;
    left: 12px;
    top: 50%;
    transform: translateY(-50%);
    color: #94a3b8;
    pointer-events: none;
}

.filterInput {
    width: 100%;
    height: 40px;
    padding: 0 12px 0 40px;
    border: 1px solid #e2e8f0;
    border-radius: 10px;
    font-size: 14px;
    background: #ffffff;
    transition: all 0.2s ease;
}

.filterInput:focus {
    outline: none;
    border-color: #3b82f6;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.table-wrapper {
    overflow-x: auto;
    padding: 4px 16px 0;
}

.data-table {
    width: 100%;
    table-layout: auto;
    border-collapse: separate;
    border-spacing: 0;
}

.data-table th {
    padding: 12px 16px;
    font-size: 12px;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    color: #64748b;
    background: #f8fafc;
    border-bottom: 1px solid #e2e8f0;
    white-space: nowrap;
    text-align: left;
}

.data-table th:first-child {
    border-top-left-radius: 8px;
}

.data-table th:last-child {
    border-top-right-radius: 8px;
}

.data-table td {
    padding: 12px 16px;
    border-bottom: 1px solid #f1f5f9;
    vertical-align: middle;
}

.table-row-hover {
    transition: background-color 0.2s ease;
}

.table-row-hover:hover {
    background-color: #f8fafc;
}

.empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 48px 24px;
    text-align: center;
}

.empty-icon {
    width: 64px;
    height: 64px;
    border-radius: 50%;
    background: linear-gradient(135deg, #f1f5f9, #e2e8f0);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #94a3b8;
    margin-bottom: 16px;
}

.empty-title {
    font-size: 16px;
    font-weight: 600;
    color: #475569;
    margin: 0 0 8px;
}

.empty-text {
    font-size: 14px;
    color: #94a3b8;
    margin: 0;
}

/* ==================== AVATAR COMPONENT ==================== */
.avatar-circle {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    overflow: hidden;
    transition: transform 0.2s ease, box-shadow 0.2s ease;
    border: 2px solid #e2e8f0;
}

.avatar-circle:hover {
    transform: scale(1.05);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.avatar-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.avatar-initials {
    font-size: 16px;
    font-weight: 600;
    color: #1e293b;
}

/* ==================== CONTACT INFO ==================== */
.contact-info {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.contact-item {
    font-size: 13px;
    color: #64748b;
}

/* ==================== ADDRESS ==================== */
.address-text {
    font-size: 14px;
    color: #64748b;
    cursor: help;
    max-width: 150px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    display: inline-block;
}

.address-text:hover {
    color: #3b82f6;
}

/* ==================== BADGES ==================== */
.badge {
    display: inline-block;
    padding: 6px 12px;
    border-radius: 20px;
    font-size: 13px;
    font-weight: 500;
    white-space: nowrap;
}

.badge-type {
    background-color: #f1f5f9;
    color: #64748b;
}

.badge-google {
    background-color: #fef3c7;
    color: #d97706;
}

.badge-normal {
    background-color: #e2e8f0;
    color: #64748b;
}

.badge-role {
    background-color: #f1f5f9;
    color: #64748b;
}

.badge-admin {
    background-color: #dbeafe;
    color: #1d4ed8;
}

.badge-user {
    background-color: #f3f4f6;
    color: #6b7280;
}

.badge-status {
    background-color: #f1f5f9;
    color: #64748b;
}

.badge-active {
    background-color: #dcfce7;
    color: #166534;
}

.badge-locked {
    background-color: #fee2e2;
    color: #dc2626;
}

/* ==================== ACTION BUTTONS ==================== */
.actionButtons {
    display: flex;
    gap: 8px;
}

.actionBtn {
    width: 36px;
    height: 36px;
    border: none;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.2s ease;
    background-color: transparent;
}

.actionEditBtn {
    color: #3b82f6;
}

.actionEditBtn:hover {
    background-color: #dbeafe;
    transform: translateY(-2px);
}

.actionResetBtn {
    color: #f59e0b;
}

.actionResetBtn:hover {
    background-color: #fef3c7;
    transform: translateY(-2px);
}

.actionDeleteBtn {
    color: #ef4444;
}

.actionDeleteBtn:hover {
    background-color: #fee2e2;
    transform: translateY(-2px);
}

/* ==================== FONT WEIGHT ==================== */
.font-semibold {
    font-weight: 600;
}

/* ==================== HIDE FULLNAME COLUMN ==================== */
/* Hide Họ tên column (3rd column: Ảnh=1, Username=2, Họ tên=3) */
.data-table thead tr th:nth-child(3),
.data-table tbody tr td:nth-child(3) {
    display: none;
}

/* ==================== CUSTOM HEADER STYLES ==================== */
.admin-account-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 24px;
    padding: 0;
}

.header-left {
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.page-title {
    margin: 0;
    font-size: 24px;
    font-weight: 700;
    color: #1e293b;
}

.filter-results {
    font-size: 14px;
    color: #64748b;
}

.header-right {
    display: flex;
    align-items: center;
    gap: 12px;
}

/* ==================== CUSTOM SEARCH BAR STYLES ==================== */
:deep(.filterGroup.filterSearch) {
    height: 40px;
    border-radius: 10px;
    background-color: #ffffff;
    border: 1px solid #e2e8f0;
    transition: all 0.2s ease;
}

:deep(.filterGroup.filterSearch:hover) {
    border-color: #cbd5e1;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

:deep(.filterGroup.filterSearch:focus-within) {
    border-color: #3b82f6;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

:deep(.filterGroup.filterSearch .filterIcon) {
    margin-left: 12px;
    color: #94a3b8;
}

:deep(.filterGroup.filterSearch .filterInput) {
    height: 38px;
    border: none;
    background: transparent;
    font-size: 14px;
    padding: 0 12px 0 8px;
}

/* ==================== CUSTOM ADD BUTTON STYLES ==================== */
.add-account-btn {
    height: 40px;
    padding: 0 20px;
    border-radius: 10px;
    background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
    color: white;
    border: none;
    font-size: 14px;
    font-weight: 600;
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    transition: all 0.2s ease;
    box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.add-account-btn:hover {
    background: linear-gradient(135deg, #2563eb 0%, #1d4ed8 100%);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.add-account-btn:active {
    transform: translateY(0);
}
</style>

<style>
.swal-high-zindex {
    z-index: 99999 !important;
}

.swal2-container.swal2-shown {
    z-index: 99999 !important;
}

.swal2-popup.swal-high-zindex {
    z-index: 999999 !important;
}
</style>
