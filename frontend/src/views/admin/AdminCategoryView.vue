<script setup>
import {computed, nextTick, ref, onMounted, onUnmounted} from "vue";
import Swal from "sweetalert2";
import "sweetalert2/dist/sweetalert2.min.css";
import {AdminCategoryPage} from "@/legacy/pages";
import AdminLayout from "@/components/AdminLayout.vue";
import AdminTableContainer from "@/components/AdminTableContainer.vue";

const {rows, modal, openEdit, save: originalSave, remove} = AdminCategoryPage.setup();
const formRef = ref(null);
const modalOpen = ref(false);
const searchQuery = ref("");

const resetForm = () => {
    modal.id = "";
    modal.name = "";
    modal.editing = false;
};

const submit = async () => {
    await save();
    resetForm();
    modalOpen.value = false;
};

const openCreate = () => {
    resetForm();
    modal.editing = false;
    modalOpen.value = true;
};

const editCategory = async (id) => {
    await openEdit(id);
    modalOpen.value = true;
};

const closeModal = () => {
    modalOpen.value = false;
    resetForm();
};

const filteredRows = computed(() => {
    let result = rows.value || [];
    
    if (searchQuery.value.trim()) {
        const query = searchQuery.value.toLowerCase();
        result = result.filter(c => 
            c.id?.toLowerCase().includes(query) || 
            c.name?.toLowerCase().includes(query)
        );
    }
    
    return result;
});

const hasFilters = computed(() => searchQuery.value.trim() !== "");

const resetFilters = () => {
    searchQuery.value = "";
};

const handleDelete = async (id) => {
    if (!id) return;

    const result = await Swal.fire({
        title: "Bạn có chắc chắn muốn xóa?",
        html: `Danh mục <strong>"${id}"</strong> sẽ bị xóa. Hành động này không thể hoàn tác.`,
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

        await remove(id);

        Swal.fire({
            title: "Đã xóa!",
            text: `Danh mục đã được xóa thành công.`,
            icon: "success",
            timer: 3000,
            timerProgressBar: true
        });
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
    closeModal();
};

const enhancedOpenCreate = () => {
    document.addEventListener('keydown', handleEscKey);
    openCreate();
};

// Override save with SweetAlert2 error handling
const save = async () => {
    try {
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
            title: modal.editing ? "Cập nhật thành công!" : "Thêm danh mục thành công!",
            icon: "success",
            customClass: { popup: "swal-high-zindex" }
        });
        
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
        <AdminTableContainer
            title="Quản lý danh mục"
            buttonText="Thêm danh mục"
            :has-active-filters="hasFilters"
            :filter-results="hasFilters ? 'Kết quả tìm kiếm: ' + searchQuery : ''"
            :is-empty="!filteredRows.length"
            @add="enhancedOpenCreate"
        >
            <template #filters>
                <div class="filterBar">
                    <div class="filterGroup filterSearch">
                        <svg class="filterIcon" width="16" height="16" viewBox="0 0 16 16" fill="none">
                            <path d="M7.33333 12.6667C3.65143 12.6667 0.666667 9.6819 0.666667 6C0.666667 2.3181 3.65143 -0.666667 7.33333 -0.666667C11.0152 -0.666667 14 2.3181 14 6C14 9.6819 11.0152 12.6667 7.33333 12.6667Z" stroke="currentColor" stroke-width="2"/>
                            <path d="M12 12L15.3333 15.3333" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        </svg>
                        <input v-model="searchQuery" class="filterInput" placeholder="Tìm theo mã hoặc tên danh mục...">
                    </div>
                </div>
            </template>
            
            <template #tableHeader>
                <tr>
                    <th>Mã loại</th>
                    <th>Tên loại</th>
                    <th>Thao tác</th>
                </tr>
            </template>

            <template #tableBody>
                <tr v-for="c in filteredRows" :key="c.id">
                    <td>{{ c.id }}</td>
                    <td>{{ c.name }}</td>
                    <td>
                        <div class="actionButtons">
                            <button class="actionBtn actionEditBtn" type="button" @click="editCategory(c.id)">✏️</button>
                            <button class="actionBtn actionDeleteBtn" type="button" @click="handleDelete(c.id)">🗑️</button>
                        </div>
                    </td>
                </tr>
            </template>
        </AdminTableContainer>
        
        <div class="modal-backdrop categoryModalStatic" :class="{open: modalOpen}" v-if="modalOpen" @click.self="enhancedCloseModal">
            <div class="admin-modal-panel">
                <form @submit.prevent="save">
                    <div class="modal-header">
                        <h4>{{ modal.editing ? "Cập nhật danh mục" : "Thêm danh mục" }}</h4>
                        <button type="button" class="btn btn-outline-primary" @click="enhancedCloseModal">Đóng</button>
                    </div>
                    <div class="admin-form-grid">
                        <div class="form-group">
                            <label>Mã loại</label>
                            <input type="text" v-model="modal.id" :readonly="modal.editing" required class="form-control" autocomplete="off">
                        </div>
                        <div class="form-group">
                            <label>Tên loại</label>
                            <input type="text" v-model="modal.name" required class="form-control" autocomplete="off">
                        </div>
                        <div class="admin-form-actions">
                            <button class="btn btn-primary" type="submit">{{ modal.editing ? "Cập nhật" : "Thêm" }}</button>
                            <button class="btn btn-outline-primary" type="button" @click="enhancedCloseModal">Huỷ</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </AdminLayout>
</template>

<style scoped>
.categoryModalStatic {
    pointer-events: none;
}

.categoryModalStatic .admin-modal-panel {
    pointer-events: auto;
}

/* ==================== RESPONSIVE MODALS ==================== */
@media (max-width: 768px) {
    .admin-modal-panel {
        width: 95vw;
        max-width: none;
        margin: 10px;
        max-height: calc(100vh - 20px);
        overflow-y: auto;
    }
    
    .admin-modal-panel h4 {
        font-size: 1rem;
    }
    
    .admin-form-grid {
        grid-template-columns: 1fr;
        gap: 12px;
    }
    
    .form-group {
        margin-bottom: 12px;
    }
    
    .form-group label {
        font-size: 14px;
    }
    
    .form-control {
        font-size: 14px;
        padding: 10px;
    }
    
    .admin-form-actions {
        flex-direction: column;
    }
    
    .admin-form-actions .btn {
        width: 100%;
    }
}

@media (max-width: 480px) {
    .admin-modal-panel {
        width: 95vw;
        margin: 5px;
    }
    
    .admin-modal-panel h4 {
        font-size: 0.9rem;
    }
    
    .form-group label {
        font-size: 14px;
    }
    
    .form-control {
        font-size: 14px;
    }
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
