<script setup>
import { computed, ref, watch } from "vue";

const props = defineProps({
    show: { type: Boolean, default: false },
    categories: { type: Array, default: () => [] },
    defaultSizes: { type: Array, default: () => [
        { id: 'S', name: 'S' },
        { id: 'M', name: 'M' },
        { id: 'L', name: 'L' },
        { id: 'XL', name: 'XL' },
        { id: '2XL', name: '2XL' },
        { id: '3XL', name: '3XL' }
    ]}
});

const emit = defineEmits(['close', 'submit']);

// ==================== STEP MANAGEMENT ====================
const currentStep = ref(1);
const totalSteps = 3;
const stepLabels = ['Thông tin', 'Kho hàng', 'Hình ảnh'];

const goToStep = (step) => {
    if (step >= 1 && step <= totalSteps) {
        currentStep.value = step;
    }
};

const nextStep = () => {
    if (currentStep.value < totalSteps) {
        currentStep.value++;
    }
};

const prevStep = () => {
    if (currentStep.value > 1) {
        currentStep.value--;
    }
};

// ==================== FORM DATA ====================
const formData = ref({
    // Step 1: Basic Info
    name: '',
    price: 0,
    discount: 0,
    categoryId: '',
    description: '',
    
    // Step 2: Inventory
    sizeQtyMap: {},
    
    // Step 3: Images
    image: null,
    imagePreview: null
});

// ==================== DYNAMIC SIZES ====================
const dynamicSizes = ref([...props.defaultSizes]);
const showAddSizeModal = ref(false);
const newSizeName = ref('');

// Initialize size quantities
const ensureSizeInputs = () => {
    dynamicSizes.value.forEach(size => {
        if (formData.value.sizeQtyMap[size.id] === undefined) {
            formData.value.sizeQtyMap[size.id] = 0;
        }
    });
};

// Total quantity computed
const totalQuantity = computed(() => {
    return Object.values(formData.value.sizeQtyMap).reduce((sum, qty) => {
        return sum + (Number(qty) || 0);
    }, 0);
});

// Add new size
const openAddSizeModal = () => {
    newSizeName.value = '';
    showAddSizeModal.value = true;
};

const closeAddSizeModal = () => {
    showAddSizeModal.value = false;
    newSizeName.value = '';
};

const confirmAddSize = () => {
    const sizeName = newSizeName.value.trim().toUpperCase();
    if (sizeName && !dynamicSizes.value.find(s => s.id === sizeName)) {
        const newSize = { id: sizeName, name: sizeName };
        dynamicSizes.value.push(newSize);
        formData.value.sizeQtyMap[sizeName] = 0;
    }
    closeAddSizeModal();
};

// ==================== IMAGE UPLOAD ====================
const handleImageUpload = (event) => {
    const file = event.target.files[0];
    if (file) {
        formData.value.image = file;
        formData.value.imagePreview = URL.createObjectURL(file);
    }
};

const removeImage = () => {
    formData.value.image = null;
    if (formData.value.imagePreview) {
        URL.revokeObjectURL(formData.value.imagePreview);
    }
    formData.value.imagePreview = null;
};

// ==================== SUBMIT ====================
const handleSubmit = () => {
    const submitData = {
        name: formData.value.name,
        price: Number(formData.value.price),
        discount: Number(formData.value.discount),
        categoryId: formData.value.categoryId,
        description: formData.value.description,
        sizes: dynamicSizes.value.map(size => ({
            id: size.id,
            name: size.name,
            quantity: Number(formData.value.sizeQtyMap[size.id]) || 0
        })),
        totalQuantity: totalQuantity.value,
        image: formData.value.image
    };
    
    emit('submit', submitData);
    resetForm();
};

// ==================== RESET ====================
const resetForm = () => {
    currentStep.value = 1;
    formData.value = {
        name: '',
        price: 0,
        discount: 0,
        categoryId: '',
        description: '',
        sizeQtyMap: {},
        image: null,
        imagePreview: null
    };
    dynamicSizes.value = [...props.defaultSizes];
    ensureSizeInputs();
};

// ==================== CLOSE ====================
const handleClose = () => {
    emit('close');
    resetForm();
};

// Initialize on mount
watch(() => props.show, (newVal) => {
    if (newVal) {
        ensureSizeInputs();
    }
});
</script>

<template>
    <teleport to="body">
        <div v-if="show" class="modalBackdrop" @click.self="handleClose">
            <div class="modalPanel" role="dialog" aria-modal="true">
                <!-- Header -->
                <div class="modalHeader">
                    <h4 class="modalTitle">Thêm sản phẩm mới</h4>
                    <button type="button" class="modalCloseBtn" @click="handleClose" aria-label="Đóng">
                        <svg width="20" height="20" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M15 5L5 15M5 5L15 15"/>
                        </svg>
                    </button>
                </div>
                
                <!-- Stepper Indicator -->
                <div class="stepperIndicator">
                    <div v-for="step in totalSteps" :key="step" class="stepperStep" 
                         :class="{
                             'stepperActive': currentStep === step,
                             'stepperCompleted': currentStep > step,
                             'stepperClickable': step <= currentStep
                         }"
                         @click="goToStep(step)">
                        <div class="stepperCircle">
                            <svg v-if="currentStep > step" width="14" height="14" viewBox="0 0 14 14" fill="none">
                                <path d="M2 7L5.5 10.5L12 3.5" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                            </svg>
                            <span v-else>{{ step }}</span>
                        </div>
                        <div class="stepperLabel">{{ stepLabels[step - 1] }}</div>
                        <div v-if="step < totalSteps" class="stepperConnector">
                            <div class="stepperConnectorLine" :class="{ 'stepperConnectorCompleted': currentStep > step }"></div>
                        </div>
                    </div>
                </div>
                
                <!-- Step Content -->
                <div class="stepperContent">
                    <!-- STEP 1: Thông tin cơ bản -->
                    <div v-show="currentStep === 1" class="stepperPane">
                        <div class="formGrid">
                            <!-- Row 1: 3 columns -->
                            <div class="formRow formRow3Cols">
                                <div class="formGroup">
                                    <label class="formLabel">TÊN SẢN PHẨM <span class="required">*</span></label>
                                    <input 
                                        v-model="formData.name" 
                                        type="text" 
                                        class="formInput" 
                                        placeholder="Nhập tên sản phẩm"
                                        required
                                    >
                                </div>
                                <div class="formGroup">
                                    <label class="formLabel">DANH MỤC <span class="required">*</span></label>
                                    <select v-model="formData.categoryId" class="formSelect" required>
                                        <option value="">Chọn danh mục</option>
                                        <option v-for="cat in categories" :key="cat.id" :value="cat.id">
                                            {{ cat.name }}
                                        </option>
                                    </select>
                                </div>
                                <div class="formGroup">
                                    <label class="formLabel">GIÁ BÁN <span class="required">*</span></label>
                                    <input 
                                        v-model.number="formData.price" 
                                        type="number" 
                                        class="formInput" 
                                        placeholder="0 VNĐ"
                                        min="0"
                                        required
                                    >
                                </div>
                            </div>
                            
                            <!-- Row 2: Giảm giá under Giá bán -->
                            <div class="formRow formRow3Cols">
                                <div class="formGroup formGroupEmpty"></div>
                                <div class="formGroup formGroupEmpty"></div>
                                <div class="formGroup">
                                    <label class="formLabel">GIẢM GIÁ (%)</label>
                                    <input 
                                        v-model.number="formData.discount" 
                                        type="number" 
                                        class="formInput" 
                                        placeholder="0%"
                                        min="0"
                                        max="100"
                                    >
                                </div>
                            </div>
                            
                            <div class="formGroup fullWidth">
                                <label class="formLabel">MÔ TẢ SẢN PHẨM</label>
                                <textarea 
                                    v-model="formData.description" 
                                    class="formTextarea" 
                                    placeholder="Nhập mô tả sản phẩm..."
                                    rows="3"
                                ></textarea>
                            </div>
                        </div>
                    </div>
                    
                    <!-- STEP 2: Kho hàng & Dynamic Size -->
                    <div v-show="currentStep === 2" class="stepperPane">
                        <div class="inventorySection">
                            <div class="sizeGridHeader">
                                <h5 class="sectionTitle">Số lượng theo size</h5>
                                <button type="button" class="addSizeBtn" @click="openAddSizeModal">
                                    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                        <line x1="12" y1="5" x2="12" y2="19"/>
                                        <line x1="5" y1="12" x2="19" y2="12"/>
                                    </svg>
                                    Thêm Size
                                </button>
                            </div>
                            
                            <div class="sizeGrid">
                                <div v-for="size in dynamicSizes" :key="size.id" class="sizeInputCard">
                                    <label class="sizeLabel">{{ size.name }}</label>
                                    <input 
                                        v-model.number="formData.sizeQtyMap[size.id]" 
                                        type="number" 
                                        class="sizeInput"
                                        placeholder="0"
                                        min="0"
                                    >
                                </div>
                            </div>
                            
                            <div class="totalQuantityRow">
                                <span class="totalLabel">Tổng số lượng:</span>
                                <span class="totalValue">{{ totalQuantity }}</span>
                            </div>
                        </div>
                    </div>
                    
                    <!-- STEP 3: Hình ảnh -->
                    <div v-show="currentStep === 3" class="stepperPane">
                        <div class="imageUploadSection">
                            <div class="uploadArea" :class="{ 'hasImage': formData.imagePreview }">
                                <input 
                                    type="file" 
                                    id="productImage" 
                                    accept="image/*" 
                                    class="fileInput"
                                    @change="handleImageUpload"
                                >
                                <label for="productImage" class="uploadLabel">
                                    <div v-if="!formData.imagePreview" class="uploadPlaceholder">
                                        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                                            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                                            <circle cx="8.5" cy="8.5" r="1.5"/>
                                            <polyline points="21 15 16 10 5 21"/>
                                        </svg>
                                        <span>Nhấn để chọn ảnh</span>
                                        <small>Hoặc kéo thả ảnh vào đây</small>
                                    </div>
                                    <div v-else class="imagePreviewWrapper">
                                        <img :src="formData.imagePreview" alt="Preview" class="imagePreview">
                                        <button type="button" class="removeImageBtn" @click.prevent="removeImage">
                                            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                                <line x1="18" y1="6" x2="6" y2="18"/>
                                                <line x1="6" y1="6" x2="18" y2="18"/>
                                            </svg>
                                        </button>
                                    </div>
                                </label>
                            </div>
                        </div>
                    </div>
                </div>
                
                <!-- Footer Actions -->
                <div class="modalFooter">
                    <button 
                        v-if="currentStep > 1" 
                        type="button" 
                        class="btnBack"
                        @click="prevStep"
                    >
                        Quay lại
                    </button>
                    <div class="spacer"></div>
                    <button 
                        v-if="currentStep < totalSteps" 
                        type="button" 
                        class="btnNext"
                        @click="nextStep"
                    >
                        LƯU VÀ TIẾP THEO
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <polyline points="9 18 15 12 9 6"/>
                        </svg>
                    </button>
                    <button 
                        v-else 
                        type="button" 
                        class="btnSubmit"
                        @click="handleSubmit"
                    >
                        Xác nhận
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <polyline points="20 6 9 17 4 12"/>
                        </svg>
                    </button>
                </div>
            </div>
            
            <!-- Add Size Modal -->
            <div v-if="showAddSizeModal" class="sizeModalBackdrop" @click.self="closeAddSizeModal">
                <div class="sizeModalPanel">
                    <h5>Thêm size mới</h5>
                    <input 
                        v-model="newSizeName" 
                        type="text" 
                        class="sizeModalInput" 
                        placeholder="VD: 4XL, Size 30"
                        @keyup.enter="confirmAddSize"
                    >
                    <div class="sizeModalActions">
                        <button type="button" class="btnCancel" @click="closeAddSizeModal">Hủy</button>
                        <button type="button" class="btnConfirm" @click="confirmAddSize">Thêm</button>
                    </div>
                </div>
            </div>
        </div>
    </teleport>
</template>

<style scoped>
/* ==================== MODAL BACKDROP ==================== */
.modalBackdrop {
    position: fixed;
    inset: 0;
    background: rgba(15, 23, 42, 0.6);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1050;
    padding: 20px;
    animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
    from { opacity: 0; }
    to { opacity: 1; }
}

/* ==================== MODAL PANEL ==================== */
.modalPanel {
    width: min(700px, 94vw);
    height: min(580px, 90vh);
    max-height: 600px;
    background: rgba(255, 255, 255, 0.98);
    border-radius: 20px;
    box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
    backdrop-filter: blur(20px);
    border: 1px solid rgba(255, 255, 255, 0.6);
    display: flex;
    flex-direction: column;
    overflow: hidden;
    animation: slideUp 0.3s ease;
}

@keyframes slideUp {
    from { 
        opacity: 0;
        transform: translateY(20px) scale(0.98);
    }
    to { 
        opacity: 1;
        transform: translateY(0) scale(1);
    }
}

/* ==================== MODAL HEADER ==================== */
.modalHeader {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);
    flex-shrink: 0;
}

.modalTitle {
    font-size: 1.125rem;
    font-weight: 600;
    color: #1e293b;
    margin: 0;
}

.modalCloseBtn {
    background: rgba(0, 0, 0, 0.05);
    border: none;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.2s ease;
    color: #64748b;
}

.modalCloseBtn:hover {
    background: rgba(0, 0, 0, 0.1);
    color: #1e293b;
}

/* ==================== STEPPER INDICATOR ==================== */
.stepperIndicator {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);
    flex-shrink: 0;
    background: linear-gradient(135deg, rgba(241, 245, 249, 0.5), rgba(226, 232, 240, 0.5));
}

.stepperStep {
    display: flex;
    flex-direction: column;
    align-items: center;
    position: relative;
}

.stepperCircle {
    width: 40px;
    height: 40px;
    border-radius: 50%;
    background: #e2e8f0;
    color: #1e293b;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1rem;
    font-weight: 700;
    transition: all 0.3s ease;
    border: 2px solid transparent;
}

.stepperActive .stepperCircle {
    background: linear-gradient(135deg, #8b5cf6, #3b82f6);
    color: white;
    box-shadow: 0 4px 12px rgba(139, 92, 246, 0.4);
}

.stepperCompleted .stepperCircle {
    background: linear-gradient(135deg, #10b981, #22c55e);
    color: white;
}

.stepperClickable {
    cursor: pointer;
}

.stepperLabel {
    font-size: 0.6875rem;
    color: #64748b;
    margin-top: 6px;
    font-weight: 500;
    white-space: nowrap;
}

.stepperActive .stepperLabel {
    color: #8b5cf6;
    font-weight: 600;
}

.stepperCompleted .stepperLabel {
    color: #10b981;
}

.stepperConnector {
    position: absolute;
    top: 16px;
    left: 100%;
    width: 60px;
    height: 2px;
    margin-left: 0;
}

.stepperConnectorLine {
    width: 100%;
    height: 100%;
    background: #e2e8f0;
    transition: all 0.3s ease;
}

.stepperConnectorCompleted {
    background: linear-gradient(90deg, #10b981, #22c55e);
}

/* ==================== STEPPER CONTENT ==================== */
.stepperContent {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
}

.stepperPane {
    height: 100%;
    animation: fadeInPane 0.3s ease;
}

@keyframes fadeInPane {
    from { opacity: 0; transform: translateX(10px); }
    to { opacity: 1; transform: translateX(0); }
}

/* ==================== FORM STYLES ==================== */
.formGrid {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.formRow {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 16px;
}

.formRow.formRow3Cols {
    grid-template-columns: 1fr 1fr 1fr;
}

.formGroup {
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.formGroup.fullWidth {
    grid-column: 1 / -1;
}

.formGroup.formGroupEmpty {
    visibility: hidden;
}

.formLabel {
    font-size: 0.8125rem;
    font-weight: 500;
    color: #374151;
}

.required {
    color: #ef4444;
}

.formInput, .formSelect, .formTextarea {
    padding: 10px 14px;
    border: 1px solid #e5e7eb;
    border-radius: 10px;
    font-size: 0.875rem;
    background: white;
    transition: all 0.2s ease;
    outline: none;
}

.formInput:focus, .formSelect:focus, .formTextarea:focus {
    border-color: #8b5cf6;
    box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.1);
}

.formTextarea {
    resize: none;
    min-height: 80px;
}

/* ==================== INVENTORY SECTION ==================== */
.inventorySection {
    height: 100%;
    display: flex;
    flex-direction: column;
}

.sizeGridHeader {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
}

.sectionTitle {
    font-size: 0.9375rem;
    font-weight: 600;
    color: #1e293b;
    margin: 0;
}

.addSizeBtn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 14px;
    background: linear-gradient(135deg, #8b5cf6, #3b82f6);
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 0.8125rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
}

.addSizeBtn:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(139, 92, 246, 0.4);
}

.sizeGrid {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
    margin-bottom: 20px;
}

.sizeInputCard {
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-radius: 10px;
    padding: 12px;
    display: flex;
    flex-direction: column;
    gap: 8px;
}

.sizeLabel {
    font-size: 0.875rem;
    font-weight: 600;
    color: #475569;
    text-align: center;
}

.sizeInput {
    padding: 8px 12px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    font-size: 0.875rem;
    text-align: center;
    background: white;
    outline: none;
    transition: all 0.2s ease;
}

.sizeInput:focus {
    border-color: #8b5cf6;
    box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.1);
}

.totalQuantityRow {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    background: linear-gradient(135deg, rgba(139, 92, 246, 0.1), rgba(59, 130, 246, 0.1));
    border-radius: 10px;
    margin-top: auto;
}

.totalLabel {
    font-size: 0.9375rem;
    font-weight: 500;
    color: #475569;
}

.totalValue {
    font-size: 1.25rem;
    font-weight: 700;
    color: #8b5cf6;
}

/* ==================== IMAGE UPLOAD ==================== */
.imageUploadSection {
    height: 100%;
    display: flex;
    flex-direction: column;
}

.uploadArea {
    flex: 1;
    border: 2px dashed #e2e8f0;
    border-radius: 16px;
    transition: all 0.3s ease;
    overflow: hidden;
    position: relative;
}

.uploadArea:hover {
    border-color: #8b5cf6;
    background: rgba(139, 92, 246, 0.02);
}

.uploadArea.hasImage {
    border-style: solid;
    border-color: #e2e8f0;
}

.fileInput {
    position: absolute;
    width: 100%;
    height: 100%;
    opacity: 0;
    cursor: pointer;
    z-index: 2;
}

.uploadLabel {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    padding: 40px;
    cursor: pointer;
}

.uploadPlaceholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 12px;
    color: #94a3b8;
}

.uploadPlaceholder span {
    font-size: 0.9375rem;
    font-weight: 500;
    color: #64748b;
}

.uploadPlaceholder small {
    font-size: 0.75rem;
    color: #94a3b8;
}

.imagePreviewWrapper {
    position: relative;
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
}

.imagePreview {
    max-width: 100%;
    max-height: 280px;
    object-fit: contain;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.removeImageBtn {
    position: absolute;
    top: 10px;
    right: 10px;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: rgba(239, 68, 68, 0.9);
    color: white;
    border: none;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.2s ease;
    z-index: 3;
}

.removeImageBtn:hover {
    background: #dc2626;
    transform: scale(1.1);
}

/* ==================== MODAL FOOTER ==================== */
.modalFooter {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-top: 1px solid rgba(0, 0, 0, 0.06);
    background: #f8fafc;
    flex-shrink: 0;
}

.spacer {
    flex: 1;
}

.btnBack {
    padding: 10px 20px;
    background: #f1f5f9;
    color: #64748b;
    border: none;
    border-radius: 10px;
    font-size: 0.875rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
}

.btnBack:hover {
    background: #e2e8f0;
    color: #475569;
}

.btnNext, .btnSubmit {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 24px;
    background: linear-gradient(135deg, #8b5cf6, #3b82f6);
    color: white;
    border: none;
    border-radius: 10px;
    font-size: 0.875rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
}

.btnNext:hover, .btnSubmit:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(139, 92, 246, 0.4);
}

/* ==================== SIZE MODAL ==================== */
.sizeModalBackdrop {
    position: fixed;
    inset: 0;
    background: rgba(15, 23, 42, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1100;
    padding: 20px;
    animation: fadeIn 0.2s ease;
}

.sizeModalPanel {
    width: min(320px, 90vw);
    background: white;
    border-radius: 16px;
    padding: 20px;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
    animation: slideUp 0.2s ease;
}

.sizeModalPanel h5 {
    margin: 0 0 16px 0;
    font-size: 1rem;
    font-weight: 600;
    color: #1e293b;
}

.sizeModalInput {
    width: 100%;
    padding: 12px 16px;
    border: 1px solid #e2e8f0;
    border-radius: 10px;
    font-size: 0.875rem;
    margin-bottom: 16px;
    outline: none;
    transition: all 0.2s ease;
}

.sizeModalInput:focus {
    border-color: #8b5cf6;
    box-shadow: 0 0 0 3px rgba(139, 92, 246, 0.1);
}

.sizeModalActions {
    display: flex;
    gap: 12px;
}

.btnCancel {
    flex: 1;
    padding: 10px 16px;
    background: #f1f5f9;
    color: #64748b;
    border: none;
    border-radius: 8px;
    font-size: 0.875rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
}

.btnCancel:hover {
    background: #e2e8f0;
}

.btnConfirm {
    flex: 1;
    padding: 10px 16px;
    background: linear-gradient(135deg, #8b5cf6, #3b82f6);
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 0.875rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.2s ease;
}

.btnConfirm:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(139, 92, 246, 0.4);
}

/* ==================== RESPONSIVE ==================== */
@media (max-width: 640px) {
    .modalPanel {
        width: 100%;
        height: 100%;
        max-height: 100vh;
        border-radius: 0;
    }
    
    .formRow {
        grid-template-columns: 1fr;
    }
    
    .sizeGrid {
        grid-template-columns: repeat(2, 1fr);
    }
    
    .stepperConnector {
        width: 40px;
    }
}
</style>
