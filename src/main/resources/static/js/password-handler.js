class PasswordHandler {
    constructor() {
        this.setupPasswordToggles();
    }

    setupPasswordToggles() {
        const passwordToggles = document.querySelectorAll('.password-toggle');
        passwordToggles.forEach(button => {
            button.addEventListener('click', () => {
                const targetId = button.getAttribute('data-target');
                const input = document.getElementById(targetId);
                const type = input.type === 'password' ? 'text' : 'password';
                input.type = type;
                button.classList.toggle('show');
            });
        });
    }
}