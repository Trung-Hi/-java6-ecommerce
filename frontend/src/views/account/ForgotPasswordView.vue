<script setup>
import {ForgotPasswordPage} from "@/legacy/pages";

const {email, message, submit} = ForgotPasswordPage.setup();
</script>

<template>
    <main class="container auth-page">
        <div class="auth-card">
            <div class="auth-card__header">
                <h1 class="auth-card__title">Quên mật khẩu</h1>
                <p class="auth-card__subtitle">Nhập email của bạn để lấy lại mật khẩu.</p>
            </div>
            <div class="auth-card__body">
                <div v-if="message" class="alert alert--error mb-3">{{ message }}</div>
                <form class="auth-form" @submit.prevent="submit">
                    <div class="form-group">
                        <label for="email">Email</label>
                        <input id="email" type="email" v-model="email" class="form-control" placeholder="Nhập email của bạn" required>
                    </div>
                    <button class="btn btn--primary btn--block" type="submit">Gửi yêu cầu</button>
                </form>
            </div>
            <div class="auth-card__footer">
                <p>Nhớ mật khẩu? <router-link to="/auth/login">Đăng nhập</router-link></p>
            </div>
        </div>
    </main>
</template>
