// Language switcher (EN | DE in the top bar).
// Keeps the current query parameters (e.g. the password reset token) when
// switching language, instead of replacing the whole query string.
document.addEventListener("click", (event) => {
    const link = event.target.closest("[data-lang]");
    if (!link) return;

    event.preventDefault();
    const url = new URL(window.location.href);
    url.searchParams.set("lang", link.dataset.lang);
    window.location.assign(url);
});
