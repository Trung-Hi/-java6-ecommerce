<script setup>
// AdminTableContainer - Reusable table container component
// Based on AdminAccountView design system

const props = defineProps({
    // Page title displayed at the top
    title: {
        type: String,
        required: true
    },
    // Add button text
    buttonText: {
        type: String,
        default: "Thêm mới"
    },
    // Loading state
    loading: {
        type: Boolean,
        default: false
    },
    // Empty state message
    emptyMessage: {
        type: String,
        default: "Không có dữ liệu"
    },
    // Whether to show empty state
    isEmpty: {
        type: Boolean,
        default: false
    },
    // Filter results text (e.g., "Hiển thị 5/10 sản phẩm")
    filterResults: {
        type: String,
        default: ""
    },
    // Whether filters are active
    hasActiveFilters: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits([
    "add",           // Click add button
    "refresh",       // Click refresh/reset filters
    "scrollToTop"    // Scroll to top of table
]);

const handleAdd = () => {
    emit("add");
};

const handleRefresh = () => {
    emit("refresh");
};
</script>

<template>
    <div class="tableContainer">
        <!-- Container Title -->
        <h3 class="containerTitle">{{ title }}</h3>
        
        <!-- Glass Card -->
        <div class="tableCard">
            <!-- Card Header -->
            <div class="cardHeader">
                <div class="headerLeft">
                    <h4>Danh sách</h4>
                    <div class="headerSearch" v-if="$slots.headerSearch">
                        <slot name="headerSearch"></slot>
                    </div>
                </div>
                <button 
                    class="addButton" 
                    type="button" 
                    @click="handleAdd"
                    :disabled="loading"
                >
                    <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <path d="M8 1V15M1 8H15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                    {{ buttonText }}
                </button>
            </div>
            
            <!-- Filter Slot -->
            <div class="filterSection" v-if="$slots.filters">
                <slot name="filters"></slot>
            </div>
            
            <!-- Filter Results Info -->
            <div v-if="hasActiveFilters && filterResults" class="filterResults">
                <span class="filterResultsText" v-html="filterResults"></span>
            </div>
            
            <!-- Table Section -->
            <div class="tableWrapper">
                <table class="dataTable" v-if="!isEmpty">
                    <!-- Table Header Slot -->
                    <thead class="tableHead">
                        <slot name="tableHeader"></slot>
                    </thead>
                    
                    <!-- Table Body Slot -->
                    <tbody class="tableBody">
                        <slot name="tableBody"></slot>
                    </tbody>
                </table>
                
                <!-- Empty State -->
                <div v-if="isEmpty" class="emptyState">
                    <div class="emptyIcon">
                        <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                            <circle cx="11" cy="11" r="8"/>
                            <path d="M21 21l-4.35-4.35"/>
                        </svg>
                    </div>
                    <h4 class="emptyTitle">{{ emptyMessage }}</h4>
                    <p class="emptyText" v-if="hasActiveFilters">Vui lòng thử điều chỉnh lại bộ lọc tìm kiếm</p>
                </div>
                
                <!-- Loading State -->
                <div v-if="loading" class="loadingState">
                    <div class="loadingSpinner"></div>
                    <span>Đang tải...</span>
                </div>
            </div>
        </div>
        
        <!-- Pagination Slot -->
        <div class="paginationSection" v-if="$slots.pagination && !isEmpty && !loading">
            <slot name="pagination"></slot>
        </div>
    </div>
</template>

<style scoped>
/* ==================== CONTAINER ==================== */
.tableContainer {
    width: 100%;
}

.containerTitle {
    font-size: 1.75rem;
    font-weight: 700;
    color: #111111;
    margin-bottom: 24px;
    padding-bottom: 12px;
    border-bottom: 2px solid #e5e5e5;
}

/* ==================== GLASS CARD ==================== */
.tableCard {
    background: #ffffff;
    border-radius: 12px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
    overflow: hidden;
    border: 1px solid #e5e5e5;
}

/* ==================== CARD HEADER ==================== */
.cardHeader {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    background: #fafafa;
    border-bottom: 1px solid #e5e5e5;
}

.cardHeader h4 {
    font-size: 1.125rem;
    font-weight: 600;
    color: #333333;
    margin: 0;
}

.headerLeft {
    display: flex;
    align-items: center;
    gap: 16px;
    flex: 1;
}

.headerLeft h4 {
    margin: 0;
    white-space: nowrap;
}

.headerSearch {
    flex: 1;
    max-width: 300px;
}

.addButton {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 10px 20px;
    background: #111111;
    color: white;
    border: none;
    border-radius: 8px;
    font-size: 0.875rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
}

.addButton:hover:not(:disabled) {
    background: #333333;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.addButton:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none;
}

/* ==================== FILTER SECTION ==================== */
.filterSection {
    padding: 12px 16px;
    background: rgba(255, 255, 255, 0.5);
    border-bottom: 1px solid rgba(226, 232, 240, 0.6);
}

/* Global filter bar styles - available to all pages using this component */
:global(.filterBar) {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 12px;
}

:global(.filterGroup) {
    display: flex;
    flex-direction: column;
    gap: 6px;
    min-width: 140px;
    flex: 0 1 auto;
}

:global(.filterGroup label) {
    font-size: 0.75rem;
    font-weight: 500;
    color: #64748b;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

:global(.filterInput),
:global(.filterSelect) {
    padding: 10px 14px;
    border: 1px solid #e2e8f0;
    border-radius: 10px;
    font-size: 0.875rem;
    background: white;
    min-width: 140px;
}

:global(.filterInput:focus),
:global(.filterSelect:focus) {
    outline: none;
    border-color: #111111;
    box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.08);
}

:global(.filterSearch) {
    position: relative;
    flex: 1;
    min-width: 240px;
    max-width: 400px;
}

:global(.filterIcon) {
    position: absolute;
    left: 12px;
    top: 50%;
    transform: translateY(-50%);
    color: #94a3b8;
    pointer-events: none;
}

:global(.filterSearch .filterInput) {
    padding-left: 40px;
    width: 100%;
}

:global(.filterResetBtn) {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 10px 16px;
    background: #f1f5f9;
    color: #64748b;
    border: 1px solid #e2e8f0;
    border-radius: 10px;
    font-size: 0.875rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
    height: fit-content;
}

:global(.filterResetBtn:hover) {
    background: #e2e8f0;
    color: #475569;
}

.filterResults {
    padding: 8px 20px 12px;
    border-bottom: 1px solid #f1f5f9;
}

.filterResultsText {
    font-size: 0.8125rem;
    color: #64748b;
}

.filterResultsText :deep(strong) {
    color: #111111;
}

/* ==================== TABLE ==================== */
.tableWrapper {
    overflow-x: auto;
    -webkit-overflow-scrolling: touch;
    margin-top: 0;
    padding: 4px 16px 0;
    min-height: 200px;
}

/* Horizontal scrollbar styling */
.tableWrapper::-webkit-scrollbar {
    height: 8px;
}

.tableWrapper::-webkit-scrollbar-track {
    background: #f1f5f9;
    border-radius: 4px;
}

.tableWrapper::-webkit-scrollbar-thumb {
    background: #94a3b8;
    border-radius: 4px;
}

.tableWrapper::-webkit-scrollbar-thumb:hover {
    background: #64748b;
}

.dataTable {
    width: 100%;
    min-width: 800px;
    border-collapse: separate;
    border-spacing: 0;
    font-size: 0.875rem;
}

/* Table Header - passed via slot */
.tableHead :deep(th) {
    padding: 12px 10px;
    font-size: 0.75rem;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    color: #64748b;
    background: #f8fafc;
    border-bottom: 1px solid #e2e8f0;
    white-space: nowrap;
    text-align: left;
}

/* Center align action column (last column) */
.tableHead :deep(th:last-child) {
    text-align: center;
}

.tableHead :deep(th:first-child) {
    border-top-left-radius: 8px;
}

.tableHead :deep(th:last-child) {
    border-top-right-radius: 8px;
}

/* Table Body - passed via slot */
.tableBody :deep(tr:hover) {
    background: rgba(241, 245, 249, 0.5);
}

.tableBody :deep(td) {
    padding: 10px 10px;
    border-bottom: 1px solid #f1f5f9;
    vertical-align: middle;
    transition: all 0.2s ease;
}

/* Center align action column cells (last column) */
.tableBody :deep(td:last-child) {
    text-align: center;
}

/* ==================== EMPTY STATE ==================== */
.emptyState {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 48px 24px;
    text-align: center;
    animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
}

.emptyIcon {
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

.emptyTitle {
    font-size: 1rem;
    font-weight: 600;
    color: #475569;
    margin: 0 0 8px;
}

.emptyText {
    font-size: 0.875rem;
    color: #94a3b8;
    margin: 0;
}

/* ==================== LOADING STATE ==================== */
.loadingState {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: 48px 24px;
    gap: 12px;
    color: #64748b;
}

.loadingSpinner {
    width: 32px;
    height: 32px;
    border: 3px solid #f5f5f5;
    border-top-color: #111111;
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    to { transform: rotate(360deg); }
}

/* ==================== PAGINATION ==================== */
.paginationSection {
    margin-top: 24px;
}

/* ==================== ACTION BUTTONS (GLOBAL) ==================== */
/* These classes can be used by child components via :deep() or directly */
:global(.actionBtn) {
    width: 34px;
    height: 34px;
    border-radius: 8px;
    border: none;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.2s ease;
}

:global(.actionBtn:hover) {
    transform: scale(1.05);
}

:global(.actionEditBtn) {
    background: #f5f5f5;
    color: #555555;
}

:global(.actionEditBtn:hover) {
    background: #111111;
    color: white;
}

:global(.actionDeleteBtn) {
    background: #fef2f2;
    color: #dc2626;
}

:global(.actionDeleteBtn:hover) {
    background: #ef4444;
    color: white;
}

:global(.actionResetBtn) {
    background: #f5f5f5;
    color: #555555;
}

:global(.actionResetBtn:hover) {
    background: #111111;
    color: white;
}

:global(.actionButtons) {
    display: flex;
    justify-content: center;
    gap: 8px;
}

/* ==================== BADGES (GLOBAL) ==================== */
:global(.statusBadge) {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 6px 12px;
    border-radius: 50px;
    font-size: 0.75rem;
    font-weight: 600;
    white-space: nowrap;
}

:global(.roleBadge) {
    display: inline-block;
    padding: 8px 16px;
    border-radius: 20px;
    font-size: 0.75rem;
    font-weight: 600;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    white-space: nowrap;
}

:global(.roleBadgeAdmin) {
    background: linear-gradient(135deg, #fef3c7, #fde68a);
    color: #92400e;
}

:global(.roleBadgeUser) {
    background: linear-gradient(135deg, #dbeafe, #bfdbfe);
    color: #1e40af;
}

/* ==================== FILTER BAR (GLOBAL) ==================== */
:global(.filterBar) {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 20px;
    margin: 12px 20px;
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(10px);
    -webkit-backdrop-filter: blur(10px);
    border-radius: 12px;
    border: 1px solid rgba(226, 232, 240, 0.8);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
    flex-wrap: wrap;
}

:global(.filterGroup) {
    display: flex;
    align-items: center;
    position: relative;
}

:global(.filterSearch) {
    flex: 1;
    min-width: 250px;
    position: relative;
}

:global(.filterIcon) {
    position: absolute;
    left: 12px;
    top: 50%;
    transform: translateY(-50%);
    color: #94a3b8;
    pointer-events: none;
}

:global(.filterInput) {
    width: 100%;
    padding: 10px 12px 10px 40px;
    border: 1px solid rgba(0, 0, 0, 0.08);
    border-radius: 10px;
    font-size: 0.875rem;
    background: rgba(255, 255, 255, 0.6);
    color: #475569;
    transition: all 0.2s ease;
    opacity: 0.9;
}

:global(.filterInput:focus) {
    outline: none;
    border-color: #3b82f6;
    background: white;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

:global(.filterInput::placeholder) {
    color: #94a3b8;
}

:global(.filterSelect) {
    padding: 10px 32px 10px 14px;
    border: 1px solid rgba(0, 0, 0, 0.08);
    border-radius: 10px;
    font-size: 0.875rem;
    background: rgba(255, 255, 255, 0.6);
    color: #475569;
    cursor: pointer;
    min-width: 140px;
    appearance: none;
    background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='16' height='16' viewBox='0 0 24 24' fill='none' stroke='%2364748b' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpolyline points='6 9 12 15 18 9'%3E%3C/polyline%3E%3C/svg%3E");
    background-repeat: no-repeat;
    background-position: right 10px center;
    transition: all 0.2s ease;
    opacity: 0.9;
}

:global(.filterSelect:focus) {
    outline: none;
    border-color: #3b82f6;
    background: white;
    box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

:global(.filterResetBtn) {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 10px 16px;
    background: rgba(241, 245, 249, 0.8);
    border: 1px solid rgba(0, 0, 0, 0.08);
    border-radius: 10px;
    font-size: 0.875rem;
    font-weight: 500;
    color: #64748b;
    cursor: pointer;
    transition: all 0.2s ease;
}

:global(.filterResetBtn:hover) {
    background: rgba(239, 68, 68, 0.1);
    border-color: rgba(239, 68, 68, 0.2);
    color: #ef4444;
}

/* ==================== PAGINATION (GLOBAL) ==================== */
:global(.paginationWrapper) {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 8px;
    margin-top: 24px;
}

:global(.pagination) {
    display: flex;
    gap: 8px;
    list-style: none;
    padding: 0;
    margin: 0;
}

:global(.pageItem) {
    display: inline-flex;
    padding: 8px 14px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: white;
    color: #475569;
    font-size: 0.875rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
}

:global(.pageItem.active) {
    background: #111111;
    color: white;
    border-color: #111111;
}

:global(.pageItem:hover:not(.active)) {
    border-color: #111111;
    color: #111111;
}

:global(.pageLink) {
    padding: 8px 14px;
    border: 1px solid #e2e8f0;
    border-radius: 8px;
    background: white;
    color: #475569;
    font-size: 0.875rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
}

:global(.pageLink:hover:not(:disabled)) {
    border-color: #111111;
    color: #111111;
    background: #fafafa;
}

:global(.pageLink:disabled) {
    opacity: 0.5;
    cursor: not-allowed;
    background: #f5f5f5;
}

/* ==================== RESPONSIVE ==================== */
@media (max-width: 768px) {
    .containerTitle {
        font-size: 1.5rem;
        margin-bottom: 16px;
    }
    
    .cardHeader {
        flex-direction: column;
        gap: 12px;
        align-items: stretch;
        padding: 12px 16px;
    }
    
    .cardHeader h4 {
        font-size: 1rem;
    }
    
    .addButton {
        justify-content: center;
        width: 100%;
    }
    
    .filterSection {
        padding: 12px;
    }
    
    :global(.filterBar) {
        flex-direction: column;
        align-items: stretch;
        padding: 12px;
        margin: 0;
        gap: 10px;
    }
    
    :global(.filterGroup) {
        min-width: 100%;
        flex: 1;
    }
    
    :global(.filterSearch) {
        min-width: 100%;
        max-width: 100%;
    }
    
    :global(.filterInput),
    :global(.filterSelect) {
        min-width: 100%;
        font-size: 14px;
    }
    
    .tableWrapper {
        padding: 4px 12px 0;
        overflow-x: auto;
        -webkit-overflow-scrolling: touch;
    }
    
    .dataTable {
        min-width: 600px;
        font-size: 13px;
    }
    
    .tableHead :deep(th) {
        padding: 10px 8px;
        font-size: 11px;
    }
    
    .tableBody :deep(td) {
        padding: 8px;
        font-size: 13px;
    }
    
    :global(.actionButtons) {
        justify-content: flex-start;
    }
    
    :global(.actionBtn) {
        width: 32px;
        height: 32px;
        font-size: 14px;
    }
    
    .emptyState {
        padding: 32px 16px;
    }
    
    .emptyTitle {
        font-size: 0.9rem;
    }
    
    .emptyText {
        font-size: 0.8rem;
    }
}

@media (max-width: 480px) {
    .containerTitle {
        font-size: 1.25rem;
    }
    
    .cardHeader {
        padding: 10px 12px;
    }
    
    .cardHeader h4 {
        font-size: 0.9rem;
    }
    
    .addButton {
        padding: 8px 16px;
        font-size: 0.8rem;
    }
    
    .filterSection {
        padding: 10px;
    }
    
    :global(.filterBar) {
        padding: 10px;
        gap: 8px;
    }
    
    :global(.filterInput),
    :global(.filterSelect) {
        padding: 8px 10px 8px 36px;
        font-size: 13px;
    }
    
    .dataTable {
        min-width: 500px;
        font-size: 12px;
    }
    
    .tableHead :deep(th) {
        padding: 8px 6px;
        font-size: 10px;
    }
    
    .tableBody :deep(td) {
        padding: 6px;
        font-size: 12px;
    }
    
    :global(.actionBtn) {
        width: 28px;
        height: 28px;
        font-size: 12px;
    }
    
    .paginationSection {
        margin-top: 16px;
    }
}
</style>
