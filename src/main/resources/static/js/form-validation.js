class FormValidator {
    static validatePassword(password, confirmPassword) {
        return password === confirmPassword;
    }

    static validatePesel(pesel) {
        if (!pesel) return false;
        if (!/^\d{11}$/.test(pesel)) return false;
        
        // sprawdzanie sumy kontrolnej PESEL
        const weights = [1, 3, 7, 9, 1, 3, 7, 9, 1, 3];
        let sum = 0;
        
        for (let i = 0; i < 10; i++) {
            sum += parseInt(pesel.charAt(i)) * weights[i];
        }
        
        const checkDigit = (10 - (sum % 10)) % 10;
        return checkDigit === parseInt(pesel.charAt(10));
    }

    static validatePhoneNumber(phone) {
        return /^\d{9}$/.test(phone);
    }

    static validateEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    static showNotification(message, type = 'info') {
        const notification = document.createElement('div');
        notification.className = `notification ${type}`;
        notification.innerHTML = `
            <div class="notification-content">
                <span>${message}</span>
            </div>
        `;
        document.body.appendChild(notification);
        
        setTimeout(() => {
            notification.classList.add('show');
            setTimeout(() => {
                notification.classList.remove('show');
                setTimeout(() => notification.remove(), 300);
            }, 3000);
        }, 100);
    }

    static validateForm(formData) {
        const validationResults = {
            isValid: true,
            errors: []
        };

        if (formData.email && !this.validateEmail(formData.email)) {
            validationResults.errors.push('Nieprawidłowy format adresu email');
            validationResults.isValid = false;
        }

        if (formData.phone && !this.validatePhoneNumber(formData.phone)) {
            validationResults.errors.push('Numer telefonu musi składać się z 9 cyfr');
            validationResults.isValid = false;
        }

        if (formData.pesel && !this.validatePesel(formData.pesel)) {
            validationResults.errors.push('Nieprawidłowy numer PESEL');
            validationResults.isValid = false;
        }

        if (formData.password && formData.confirmPassword && 
            !this.validatePassword(formData.password, formData.confirmPassword)) {
            validationResults.errors.push('Hasła nie są identyczne');
            validationResults.isValid = false;
        }

        return validationResults;
    }
}