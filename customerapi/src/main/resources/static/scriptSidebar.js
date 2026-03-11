const hamburger = document.getElementById('hamburgerBtn');
        const sidebar   = document.getElementById('sidebar');
        const overlay   = document.getElementById('overlay');

        // open / close sidebar
        hamburger.addEventListener('click', () => {
            hamburger.classList.toggle('open');
            sidebar.classList.toggle('open');
            overlay.classList.toggle('active');
        });

        // close with click on the overlay
        overlay.addEventListener('click', () => {
            hamburger.classList.remove('open');
            sidebar.classList.remove('open');
            overlay.classList.remove('active');
        });

        // close with click on the  link
        sidebar.querySelectorAll('a').forEach(link => {
            link.addEventListener('click', () => {
                hamburger.classList.remove('open');
                sidebar.classList.remove('open');
                overlay.classList.remove('active');
            });
        });