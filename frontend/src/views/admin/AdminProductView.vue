<script setup>
import {computed, nextTick, ref} from "vue";
import Swal from "sweetalert2";
import "sweetalert2/dist/sweetalert2.min.css";
import {AdminProductPage} from "@/legacy/pages";
import AdminLayout from "@/components/AdminLayout.vue";
import AdminTableContainer from "@/components/AdminTableContainer.vue";
import CurrencyInput from "@/components/CurrencyInput.vue";

const {state, form, editing, message, load, edit, reset, save, remove, next, prev, money} = AdminProductPage.setup();
const PRICE_MIN = 0;
const PRICE_MAX = 2000000;
const PRICE_STEP = 50000;
const modalOpen = ref(false);
const errorMessage = ref("");
const successMessage = ref("");
const modalMessage = ref("");
const modalMessageError = ref(false);

// Wizard Modal State
const wizardStep = ref(1);
const wizardTotalSteps = 3;

// Dynamic Variants State
const variants = ref([]);
const availableSizes = ref(['S', 'M', 'L', 'XL', 'XXL']);

const filters = ref({
    keyword: "",
    categoryId: "",
    minPrice: "",
    maxPrice: ""
});
const selectedPriceRange = ref("all");
const sliderMin = ref(PRICE_MIN);
const sliderMax = ref(PRICE_MAX);
const priceRanges = [
    {id: "all", label: "Tất cả", min: "", max: ""},
    {id: "lt100", label: "Dưới 100.000", min: "", max: 100000},
    {id: "100_300", label: "Từ 100k - 300k", min: 100000, max: 300000},
    {id: "300_500", label: "Từ 300k - 500k", min: 300000, max: 500000},
    {id: "gt500", label: "Trên 500k", min: 500000, max: ""}
];
const listRef = ref(null);

const displayedProducts = computed(() => state.rows || []);
const hasFilters = computed(() => {
    return filters.value.keyword?.trim() || 
           filters.value.categoryId || 
           filters.value.minPrice || 
           filters.value.maxPrice ||
           selectedPriceRange.value !== "all";
});
const visiblePages = computed(() => {
    const total = Number(state.totalPages || 0);
    const current = Number(state.page || 0);
    if (total <= 1) {
        return total === 1 ? [0] : [];
    }
    const start = Math.max(0, current - 2);
    const end = Math.min(total - 1, start + 4);
    const adjustedStart = Math.max(0, end - 4);
    return Array.from({length: end - adjustedStart + 1}, (_, index) => adjustedStart + index);
});
const sizeTotalQuantity = computed(() => (state.sizes || []).reduce((total, size) => {
    if (!size) return total;
    const qty = Number(form.sizeQtyMap?.[String(size.id)] || 0);
    return total + (Number.isFinite(qty) ? Math.max(0, qty) : 0);
}, 0));

const resolveImage = (name) => name ? `/images/${name}` : "/images/logo.png";
const formatCurrency = (val) => money(val);
const ensureSizeInputs = () => {
    if (!form.sizeQtyMap) {
        form.sizeQtyMap = {};
    }
    (state.sizes || []).forEach((size) => {
        if (!size) return;
        const key = String(size.id);
        if (form.sizeQtyMap[key] === undefined || form.sizeQtyMap[key] === null) {
            form.sizeQtyMap[key] = 0;
        }
    });
};
const openCreateModal = () => {
    reset();
    editing.value = false;
    ensureSizeInputs();
    modalMessage.value = "";
    modalMessageError.value = false;
    // Reset wizard state
    wizardStep.value = 1;
    variants.value = [];
    modalOpen.value = true;
};
const openEditModal = async (product) => {
    try {
        await edit(product.id);
        // Load variants from product
        if (product.productSizes && product.productSizes.length > 0) {
            variants.value = product.productSizes.map(ps => ({
                sizeName: ps.size?.name || '',
                quantity: ps.quantity || 0
            }));
        } else {
            variants.value = [];
        }
    } catch (e) {
        form.id = product.id;
        form.name = product.name || "";
        form.price = product.price || "";
        form.discount = product.discount || "";
        form.quantity = product.quantity || "";
        form.image = product.image || "";
        form.imageFile = null;
        form.categoryId = product.category?.id || product.categoryId || "";
        form.description = product.description || "";
        errorMessage.value = e.message || "Không tải được chi tiết sản phẩm, đang mở form với dữ liệu hiện có.";
    }
    ensureSizeInputs();
    modalMessage.value = "";
    modalMessageError.value = false;
    // Reset wizard to step 1
    wizardStep.value = 1;
    modalOpen.value = true;
};
const closeModal = () => {
    modalOpen.value = false;
    modalMessage.value = "";
    modalMessageError.value = false;
    reset();
    wizardStep.value = 1;
    variants.value = [];
};

// Wizard Navigation Functions
const nextStep = () => {
    if (wizardStep.value < wizardTotalSteps) {
        wizardStep.value++;
    }
};

const prevStep = () => {
    if (wizardStep.value > 1) {
        wizardStep.value--;
    }
};

const goToStep = (step) => {
    if (step >= 1 && step <= wizardTotalSteps) {
        wizardStep.value = step;
    }
};

// Dynamic Size Management Functions
const addVariant = () => {
    variants.value.push({
        sizeName: '',
        quantity: 0
    });
};

const removeVariant = (index) => {
    variants.value.splice(index, 1);
};

const validateVariantSize = (index, newSize) => {
    // Kiểm tra trùng size
    const duplicateCount = variants.value.filter((v, i) => 
        i !== index && v.sizeName === newSize
    ).length;
    
    if (duplicateCount > 0) {
        Swal.fire({
            icon: 'warning',
            title: 'Size đã tồn tại',
            text: 'Size này đã được chọn cho sản phẩm này. Vui lòng chọn size khác.',
            confirmButtonColor: '#111111'
        });
        // Reset về giá trị cũ
        const oldSize = variants.value[index].sizeName;
        variants.value[index].sizeName = oldSize;
        return false;
    }
    return true;
};

// Tính tổng số lượng từ variants
const totalQuantity = computed(() => {
    return variants.value.reduce((total, variant) => {
        return total + (Number(variant.quantity) || 0);
    }, 0);
});

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

const enhancedOpenCreateModal = () => {
    document.addEventListener('keydown', handleEscKey);
    openCreateModal();
};
const submitForm = async () => {
    const wasEditing = editing.value;
    // Set quantity từ sizeQtyMap total
    form.quantity = sizeTotalQuantity.value;
    form.totalQuantity = sizeTotalQuantity.value;

    // Validation
    const errors = [];
    if (!form.name?.trim()) {
        errors.push('Tên sản phẩm không được để trống');
    }
    if (!form.price || form.price <= 0) {
        errors.push('Giá sản phẩm phải lớn hơn 0');
    }
    if (!form.categoryId) {
        errors.push('Vui lòng chọn danh mục');
    }

    if (errors.length > 0) {
        await Swal.fire({
            icon: 'warning',
            title: 'Thiếu thông tin',
            html: errors.map(e => `<div>• ${e}</div>`).join(''),
            confirmButtonColor: '#111111'
        });
        return;
    }

    // Legacy system uses sizeQtyMap directly, no need to convert to variants
    // The save() function in pages.js will handle the form data including sizeQtyMap

    try {
        await save();
        await load();
        successMessage.value = wasEditing ? "Cập nhật sản phẩm thành công" : "Thêm sản phẩm thành công";
        errorMessage.value = "";
        modalMessage.value = successMessage.value;
        modalMessageError.value = false;
        modalOpen.value = false;
        // Reset wizard state
        wizardStep.value = 1;
        variants.value = [];
    } catch (e) {
        modalMessage.value = e.message || "Cập nhật thất bại";
        modalMessageError.value = true;
        errorMessage.value = modalMessage.value;
    }
};
const onImageChange = (event) => {
    const file = event?.target?.files?.[0] || null;
    form.imageFile = file;
};
const removeProduct = async (product) => {
    const result = await Swal.fire({
        title: "Bạn có chắc chắn muốn xóa?",
        html: `Sản phẩm <strong>"${product.name}"</strong> sẽ bị xóa. Hành động này không thể hoàn tác.`,
        icon: "warning",
        showCancelButton: true,
        confirmButtonText: "Xác nhận xóa",
        cancelButtonText: "Hủy",
        confirmButtonColor: "#dc2626",
        cancelButtonColor: "#6b7280",
        reverseButtons: true,
        focusCancel: true
    });
    
    if (!result.isConfirmed) {
        return;
    }
    
    try {
        await remove(product.id);
        successMessage.value = "Xoá sản phẩm thành công";
        errorMessage.value = "";
        
        Swal.fire({
            title: "Đã xóa!",
            text: "Sản phẩm đã được xóa thành công.",
            icon: "success",
            timer: 1500,
            timerProgressBar: true,
            position: "top-end",
            toast: true,
            showConfirmButton: false
        });
    } catch (e) {
        successMessage.value = "";
        errorMessage.value = e.message || "Xoá sản phẩm thất bại";
        
        Swal.fire({
            title: "Lỗi!",
            text: e.message || "Xóa sản phẩm thất bại",
            icon: "error",
            confirmButtonColor: "#dc2626"
        });
    }
};
const scrollToResults = async () => {
    await nextTick();
    listRef.value?.scrollIntoView({behavior: "smooth", block: "start"});
};
const normalizePrice = (value) => {
    if (value === "" || value === null || value === undefined) {
        return "";
    }
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : "";
};
const applySliderToFilter = () => {
    filters.value.minPrice = Math.min(sliderMin.value, sliderMax.value);
    filters.value.maxPrice = Math.max(sliderMin.value, sliderMax.value);
};
const onSliderMinInput = () => {
    if (sliderMin.value > sliderMax.value) {
        sliderMax.value = sliderMin.value;
    }
    selectedPriceRange.value = "all";
    applySliderToFilter();
};
const onSliderMaxInput = () => {
    if (sliderMax.value < sliderMin.value) {
        sliderMin.value = sliderMax.value;
    }
    selectedPriceRange.value = "all";
    applySliderToFilter();
};
const applyFilters = async () => {
    state.page = 0;
    state.keyword = filters.value.keyword?.trim() || "";
    state.categoryId = filters.value.categoryId || "";
    state.minPrice = normalizePrice(filters.value.minPrice);
    state.maxPrice = normalizePrice(filters.value.maxPrice);
    await load();
    await scrollToResults();
};
const applyPriceRange = async () => {
    const selected = priceRanges.find((range) => range.id === selectedPriceRange.value) || priceRanges[0];
    filters.value.minPrice = selected.min;
    filters.value.maxPrice = selected.max;
    sliderMin.value = selected.min === "" ? PRICE_MIN : Number(selected.min);
    sliderMax.value = selected.max === "" ? PRICE_MAX : Number(selected.max);
    await applyFilters();
};
const clearFilters = async () => {
    filters.value = {keyword: "", categoryId: "", minPrice: "", maxPrice: ""};
    selectedPriceRange.value = "all";
    sliderMin.value = PRICE_MIN;
    sliderMax.value = PRICE_MAX;
    state.page = 0;
    state.keyword = "";
    state.categoryId = "";
    state.minPrice = "";
    state.maxPrice = "";
    await load();
    await scrollToResults();
};
const goToPage = async (page) => {
    const targetPage = Number(page);
    if (!Number.isInteger(targetPage) || targetPage < 0 || targetPage >= Number(state.totalPages || 0) || targetPage === state.page) {
        return;
    }
    state.page = targetPage;
    await load();
    await scrollToResults();
};
</script>

<template>
    <AdminLayout>
        <AdminTableContainer
            title="Quản lý sản phẩm"
            buttonText="Thêm sản phẩm"
            :has-active-filters="hasFilters"
            :filter-results="hasFilters ? `Đang hiển thị ${displayedProducts.length} sản phẩm` : ''"
            :is-empty="!displayedProducts.length"
            @add="enhancedOpenCreateModal"
            @refresh="clearFilters"
        >
            <template #filters>
                <div class="filterBar">
                    <div class="filterGroup filterSearch">
                        <svg class="filterIcon" width="16" height="16" viewBox="0 0 16 16" fill="none">
                            <path d="M7.33333 12.6667C3.65143 12.6667 0.666667 9.6819 0.666667 6C0.666667 2.3181 3.65143 -0.666667 7.33333 -0.666667C11.0152 -0.666667 14 2.3181 14 6C14 9.6819 11.0152 12.6667 7.33333 12.6667Z" stroke="currentColor" stroke-width="2"/>
                            <path d="M12 12L15.3333 15.3333" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                        </svg>
                        <input v-model.trim="filters.keyword" class="filterInput" placeholder="Tìm sản phẩm...">
                    </div>
                    <div class="filterGroup">
                        <select v-model="filters.categoryId" class="filterSelect" @change="applyFilters">
                            <option value="">Tất cả danh mục</option>
                            <option v-for="c in state.categories" :key="c.id" :value="c.id">{{ c.name }}</option>
                        </select>
                    </div>
                    <div class="filterGroup">
                        <select v-model="selectedPriceRange" class="filterSelect" @change="applyPriceRange">
                            <option value="all">Tất cả giá</option>
                            <option v-for="range in priceRanges" :key="range.id" :value="range.id">{{ range.label }}</option>
                        </select>
                    </div>
                </div>
            </template>
            
            <template #tableHeader>
                <tr>
                    <th>Ảnh</th>
                    <th>Tên</th>
                    <th>Danh mục</th>
                    <th>Giá</th>
                    <th>Số lượng</th>
                    <th>Thao tác</th>
                </tr>
            </template>
            
            <template #tableBody>
                <tr v-for="product in displayedProducts" :key="product.id">
                    <td><img class="product-thumb" :src="resolveImage(product.image)" alt="Ảnh sản phẩm" style="width: 40px; height: 40px; object-fit: cover; border-radius: 6px;"></td>
                    <td>{{ product.name }}</td>
                    <td>{{ product.category?.name || product.categoryName || "-" }}</td>
                    <td>{{ formatCurrency(product.price) }}</td>
                    <td>{{ product.quantity ?? 0 }}</td>
                    <td>
                        <div class="actionButtons">
                            <button class="actionBtn actionEditBtn" type="button" @click="openEditModal(product)">✏️</button>
                            <button class="actionBtn actionDeleteBtn" type="button" @click="removeProduct(product)">🗑️</button>
                        </div>
                    </td>
                </tr>
            </template>
            
            <template #pagination v-if="state.totalPages > 1">
                <div class="paginationWrapper">
                    <button class="pageLink" @click="prev" :disabled="state.page <= 0">Trước</button>
                    <button v-for="page in visiblePages" :key="'admin-page-'+page" class="pageItem" :class="{ active: state.page === page }" @click="goToPage(page)">{{ page + 1 }}</button>
                    <button class="pageLink" @click="next" :disabled="state.page + 1 >= state.totalPages">Sau</button>
                </div>
            </template>
        </AdminTableContainer>
        <div class="modal-backdrop productModalStatic" :class="{ open: modalOpen }" @mousedown.self="enhancedCloseModal">
            <div class="admin-modal-panel wizard-modal">
                <div class="modal-header">
                    <h4>{{ editing ? "Chỉnh sửa sản phẩm" : "Thêm sản phẩm" }}</h4>
                    <button type="button" class="btn btn-secondary" @click="enhancedCloseModal">Đóng</button>
                </div>
                
                <!-- Wizard Steps Indicator -->
                <div class="wizard-steps">
                    <div class="wizard-step" :class="{ active: wizardStep === 1, completed: wizardStep > 1 }" @click="goToStep(1)">
                        <div class="step-number">1</div>
                        <div class="step-label">Thông tin sản phẩm</div>
                    </div>
                    <div class="wizard-step" :class="{ active: wizardStep === 2, completed: wizardStep > 2 }" @click="goToStep(2)">
                        <div class="step-number">2</div>
                        <div class="step-label">Số lượng theo size</div>
                    </div>
                    <div class="wizard-step" :class="{ active: wizardStep === 3, completed: wizardStep > 3 }" @click="goToStep(3)">
                        <div class="step-number">3</div>
                        <div class="step-label">Ảnh & Xác nhận</div>
                    </div>
                </div>

                <form class="admin-product-form" @submit.prevent="submitForm">
                    <div v-if="modalMessage" class="status-message" :class="{ 'status-error': modalMessageError }">{{ modalMessage }}</div>
                    
                    <!-- Step 1: Basic Info -->
                    <div v-show="wizardStep === 1" class="wizard-step-content">
                        <div class="admin-form-grid">
                            <div class="form-group full-span">
                                <label>Tên sản phẩm <span class="required">*</span></label>
                                <input type="text" v-model.trim="form.name" required placeholder="Nhập tên sản phẩm">
                            </div>
                            <div class="form-group">
                                <label>Giá bán <span class="required">*</span></label>
                                <CurrencyInput
                                    v-model="form.price"
                                    placeholder="Nhập giá bán"
                                    :min="0"
                                    required
                                />
                            </div>
                            <div class="form-group">
                                <label>Giảm giá (%)</label>
                                <input type="number" step="0.01" min="0" max="100" v-model.number="form.discount" placeholder="0">
                            </div>
                            <div class="form-group full-span">
                                <label>Danh mục <span class="required">*</span></label>
                                <select v-model="form.categoryId" required>
                                    <option value="">-- Chọn danh mục --</option>
                                    <option v-for="c in state.categories" :key="'m'+c.id" :value="c.id">{{ c.name }}</option>
                                </select>
                            </div>
                            <div class="form-group full-span">
                                <label>Mô tả</label>
                                <textarea v-model.trim="form.description" rows="3" placeholder="Nhập mô tả sản phẩm"></textarea>
                            </div>
                        </div>
                    </div>

                    <!-- Step 2: Size Quantities -->
                    <div v-show="wizardStep === 2" class="wizard-step-content">
                        <div class="size-quantity-section full-width">
                            <h5>Số lượng theo size</h5>
                            <div class="size-grid">
                                <template v-for="size in (state.sizes || [])" :key="size?.id">
                                    <div v-if="size" class="size-input-group">
                                        <label>Size {{ size.name }}</label>
                                        <input type="number" min="0" v-model.number="form.sizeQtyMap[size.id]" placeholder="0">
                                    </div>
                                </template>
                            </div>
                            <div class="total-quantity-summary">
                                <strong>Tổng số lượng: {{ sizeTotalQuantity }}</strong>
                            </div>
                        </div>
                    </div>

                    <!-- Step 3: Image Upload & Review -->
                    <div v-show="wizardStep === 3" class="wizard-step-content">
                        <div class="admin-form-grid">
                            <div class="form-group full-span image-upload-section">
                                <h4 class="section-title">ẢNH SẢN PHẨM</h4>
                                <div class="image-preview-wrapper" v-if="form.image">
                                    <div class="image-preview">
                                        <img :src="resolveImage(form.image)" alt="Ảnh sản phẩm" />
                                    </div>
                                </div>
                                <div class="image-placeholder" v-if="!form.image">
                                    <div class="placeholder-box">
                                        <span>Chưa có ảnh sản phẩm</span>
                                    </div>
                                </div>
                                <div class="file-input-wrapper">
                                    <input type="file" accept="image/*" @change="onImageChange" id="productImage">
                                    <label for="productImage" class="file-input-label">Chọn ảnh</label>
                                </div>
                            </div>
                            
                            <div class="review-section full-span">
                                <h5>Xác nhận thông tin</h5>
                                <div class="review-grid">
                                    <div class="review-item">
                                        <label>Tên sản phẩm:</label>
                                        <span>{{ form.name || '-' }}</span>
                                    </div>
                                    <div class="review-item">
                                        <label>Giá bán:</label>
                                        <span>{{ formatCurrency(form.price) }}</span>
                                    </div>
                                    <div class="review-item">
                                        <label>Giảm giá:</label>
                                        <span>{{ form.discount || 0 }}%</span>
                                    </div>
                                    <div class="review-item">
                                        <label>Danh mục:</label>
                                        <span>{{ state.categories?.find(c => c.id === form.categoryId)?.name || '-' }}</span>
                                    </div>
                                    <div class="review-item">
                                        <label>Tổng số lượng:</label>
                                        <span>{{ sizeTotalQuantity }}</span>
                                    </div>
                                    <div class="review-item full-span">
                                        <label>Size:</label>
                                        <div class="variant-tags">
                                            <template v-for="size in state.sizes" :key="size?.id">
                                                <span v-if="size && form.sizeQtyMap?.[size.id] > 0" class="variant-tag">
                                                    {{ size.name }}: {{ form.sizeQtyMap[size.id] }}
                                                </span>
                                            </template>
                                            <span v-if="sizeTotalQuantity === 0">Chưa có size</span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Wizard Navigation Buttons -->
                    <div class="wizard-actions">
                        <button 
                            v-if="wizardStep > 1" 
                            type="button" 
                            class="btn btn-secondary" 
                            @click="prevStep"
                        >
                            ← Quay lại
                        </button>
                        <button 
                            v-if="wizardStep < 3" 
                            type="button" 
                            class="btn btn-dark" 
                            @click="nextStep"
                        >
                            Tiếp tục →
                        </button>
                        <button 
                            v-if="wizardStep === 3" 
                            type="submit" 
                            class="btn btn-dark"
                        >
                            {{ editing ? "Cập nhật" : "Thêm sản phẩm" }}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    </AdminLayout>
</template>

<style scoped>
.productModalStatic {
    pointer-events: none;
}

.productModalStatic .admin-modal-panel {
    pointer-events: auto;
}

/* Wizard Modal Styles */
.wizard-modal {
    max-width: 800px;
}

.wizard-steps {
    display: flex;
    justify-content: space-between;
    margin-bottom: 30px;
    padding: 0 20px;
}

.wizard-step {
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
    opacity: 0.5;
    transition: opacity 0.3s ease;
}

.wizard-step.active,
.wizard-step.completed {
    opacity: 1;
}

.step-number {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: #e2e8f0;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    margin-bottom: 8px;
    transition: all 0.3s ease;
}

.wizard-step.active .step-number {
    background: #000000;
    color: white;
}

.wizard-step.completed .step-number {
    background: #333333;
    color: white;
}

.step-label {
    font-size: 0.875rem;
    color: #64748b;
    font-weight: 500;
}

.wizard-step.active .step-label {
    color: #000000;
}

.wizard-step-content {
    min-height: 300px;
}

.wizard-actions {
    display: flex;
    justify-content: space-between;
    margin-top: 30px;
    padding-top: 20px;
    border-top: 1px solid #e2e8f0;
}

/* Size Variant Section */
.size-variant-section {
    padding: 20px;
    background: #f5f5f5;
    border-radius: 0;
}

.section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.section-header h5 {
    margin: 0;
    font-size: 1rem;
    font-weight: 600;
    color: #334155;
}

.empty-variants {
    text-align: center;
    padding: 40px;
    color: #94a3b8;
    font-style: italic;
}

.variant-row {
    display: grid;
    grid-template-columns: 1fr 1fr 50px;
    gap: 12px;
    margin-bottom: 12px;
    align-items: end;
}

.variant-size,
.variant-quantity {
    display: flex;
    flex-direction: column;
}

.variant-size label,
.variant-quantity label {
    font-size: 0.875rem;
    font-weight: 500;
    color: #64748b;
    margin-bottom: 4px;
}

.variant-size select,
.variant-quantity input {
    padding: 10px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    font-size: 0.875rem;
}

.variant-action button {
    width: 36px;
    height: 36px;
    border: none;
    border-radius: 8px;
    background: #fee2e2;
    color: #dc2626;
    cursor: pointer;
    transition: all 0.2s ease;
}

.variant-action button:hover {
    background: #dc2626;
    color: white;
}

/* Fixed Size Quantity Section */
.size-quantity-section {
    margin-top: 24px;
    padding: 20px;
    background: #f5f5f5;
    border-radius: 0;
}

.size-quantity-section.full-width {
    margin-top: 0;
    min-height: 300px;
}

.size-quantity-section h5 {
    margin: 0 0 16px 0;
    font-size: 1rem;
    font-weight: 600;
    color: #333333;
    text-transform: uppercase;
    letter-spacing: 0.05em;
}

.size-grid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
}

.size-input-group {
    display: flex;
    flex-direction: column;
}

.size-input-group label {
    font-size: 0.8rem;
    font-weight: 500;
    color: #666666;
    margin-bottom: 6px;
    text-transform: uppercase;
}

.size-input-group input {
    padding: 10px 12px;
    border: 1px solid #e0e0e0;
    border-radius: 6px;
    font-size: 0.9rem;
    background: white;
    transition: border-color 0.2s;
}

.size-input-group input:focus {
    outline: none;
    border-color: #111111;
}

@media (max-width: 768px) {
    .size-grid {
        grid-template-columns: repeat(2, 1fr);
    }
}

.total-quantity-summary {
    margin-top: 20px;
    padding: 15px;
    background: #000000;
    border-radius: 0;
    text-align: center;
    font-size: 1rem;
    color: #ffffff;
}

/* Review Section */
.review-section {
    margin-top: 20px;
    padding: 20px;
    background: #f5f5f5;
    border-radius: 0;
}

.review-section h5 {
    margin: 0 0 15px 0;
    font-size: 1rem;
    font-weight: 600;
    color: #334155;
}

.review-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 15px;
}

.review-item {
    display: flex;
    flex-direction: column;
}

.review-item.full-span {
    grid-column: 1 / -1;
}

.review-item label {
    font-size: 0.875rem;
    font-weight: 500;
    color: #64748b;
    margin-bottom: 4px;
}

.review-item span {
    font-size: 0.9375rem;
    font-weight: 600;
    color: #1e293b;
}

.variant-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
}

.variant-tag {
    padding: 6px 12px;
    background: #3b82f6;
    color: white;
    border-radius: 4px;
    font-size: 0.875rem;
    font-weight: 500;
}

.image-upload-section {
    text-align: center;
}

.section-title {
    font-size: 1rem;
    font-weight: 600;
    color: #333333;
    margin: 0 0 20px 0;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

.image-preview-wrapper {
    display: flex;
    justify-content: center;
    margin-bottom: 20px;
}

.image-preview {
    width: 200px;
    height: 200px;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid #e0e0e0;
    background: #f5f5f5;
}

.image-preview img {
    width: 100%;
    height: 100%;
    object-fit: contain;
}

.image-placeholder {
    display: flex;
    justify-content: center;
    margin-bottom: 20px;
}

.placeholder-box {
    width: 200px;
    height: 200px;
    border-radius: 8px;
    border: 2px dashed #cccccc;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #999999;
    font-size: 0.875rem;
    background: #fafafa;
}

.file-input-wrapper {
    display: flex;
    justify-content: center;
}

.file-input-wrapper input[type="file"] {
    display: none;
}

.file-input-label {
    display: inline-block;
    padding: 10px 24px;
    background: #111111;
    color: #ffffff;
    border-radius: 6px;
    font-size: 0.875rem;
    font-weight: 500;
    cursor: pointer;
    transition: background 0.2s;
}

.file-input-label:hover {
    background: #333333;
}

/* Make textarea full width like other inputs */
.admin-form-grid textarea {
    width: 100%;
    resize: vertical;
}

.required {
    color: #dc2626;
}

/* Custom Button Styles for Monochrome Theme */
.btn-dark {
    background: #111111;
    color: white;
    border: none;
    padding: 10px 20px;
    border-radius: 8px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
}

.btn-dark:hover {
    background: #333333;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.btn-secondary {
    background: #f5f5f5;
    color: #555555;
    border: 1px solid #e0e0e0;
    padding: 10px 20px;
    border-radius: 8px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
}

.btn-secondary:hover {
    background: #e8e8e8;
    color: #333333;
}

.btn-danger {
    background: #fef2f2;
    color: #dc2626;
    border: 1px solid #fecaca;
    padding: 6px 12px;
    border-radius: 6px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
}

.btn-danger:hover {
    background: #dc2626;
    color: white;
}

.btn-sm {
    padding: 6px 12px;
    font-size: 0.875rem;
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
