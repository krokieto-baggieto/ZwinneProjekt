class ModalHandler {
    constructor() {
        this.setupModalClosing();
    }

    setupModalClosing() {
        const modals = document.querySelectorAll('.modal');
        const closeButtons = document.querySelectorAll('.modal .close');

        closeButtons.forEach(button => {
            button.onclick = () => {
                button.closest('.modal').style.display = 'none';
            };
        });

        window.onclick = (event) => {
            if (event.target.classList.contains('modal')) {
                event.target.style.display = 'none';
            }
        };
    }

    static showModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.style.display = 'block';
        }
    }

    static hideModal(modalId) {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.style.display = 'none';
        }
    }
}