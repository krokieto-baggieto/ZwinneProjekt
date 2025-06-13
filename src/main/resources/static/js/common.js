// Powiadomienia
function showNotification(message, type = 'info') {
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

// Aminacje formularzy
function setupFormAnimations() {
    const inputs = document.querySelectorAll('.input-animated');
    inputs.forEach(input => {
        if (input.value) {
            input.classList.add('has-value');
        }
        
        input.addEventListener('focus', () => {
            input.classList.add('focused');
        });
        
        input.addEventListener('blur', () => {
            if (!input.value) {
                input.classList.remove('focused', 'has-value');
            }
        });
        
        input.addEventListener('input', () => {
            input.classList.toggle('has-value', !!input.value);
        });
    });
}

// Podgląd hasła
function setupPasswordToggle() {
    document.querySelectorAll('.password-toggle').forEach(button => {
        button.addEventListener('click', function() {
            const targetId = this.getAttribute('data-target');
            const input = document.getElementById(targetId);
            if (input) {
                const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
                input.setAttribute('type', type);
                this.classList.toggle('active');
                
                // Toggle eye icon
                const eyeIcon = this.querySelector('.eye-icon');
                if (eyeIcon) {
                    eyeIcon.classList.toggle('visible');
                }
            }
        });
    });
}

// Animacja tła
function setupBackgroundAnimation() {
    const backgroundEl = document.querySelector('.background-animation');
    if (backgroundEl) {
        document.addEventListener('mousemove', (e) => {
            const x = e.clientX / window.innerWidth;
            const y = e.clientY / window.innerHeight;
            
            backgroundEl.style.background = `
                linear-gradient(
                    ${45 + x * 10}deg,
                    rgba(37, 99, 235, ${0.1 + y * 0.1}),
                    rgba(59, 130, 246, ${0.1 + x * 0.1})
                )
            `;
        });
    }
}

// Form Validation Utilities
function validatePassword(passwordInput, confirmPasswordInput) {
    return passwordInput.value === confirmPasswordInput.value;
}

function validatePesel(peselInput) {
    const pesel = peselInput.value;
    return pesel.length === 11 && /^\d+$/.test(pesel);
}

function validatePhoneNumber(phoneInput) {
    const phone = phoneInput.value;
    return phone.length === 9 && /^\d+$/.test(phone);
}

// Initialize all common functionality
document.addEventListener('DOMContentLoaded', function() {
    setupFormAnimations();
    setupPasswordToggle();
    setupBackgroundAnimation();
});