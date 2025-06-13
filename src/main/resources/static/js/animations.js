class AnimationHandler {
    constructor() {
        this.setupBackgroundAnimation();
        this.setupFormAnimations();
    }

    setupBackgroundAnimation() {
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

    setupFormAnimations() {
        const inputs = document.querySelectorAll('.input-animated, .profile-input');
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
}