class HeaderContent extends HTMLElement{
    connectedCallback(){
        this.innerHTML = `
<div class="zt-topbar text-white text-xs sm:text-sm py-2 px-4 md:px-8 flex flex-wrap items-center justify-between gap-2">
  <div class="flex items-center gap-3 flex-wrap">
    <span class="flex items-center gap-1" style="color:#8FE9E1;"><i class="fas fa-truck-fast"></i> Free delivery over Rs.15,000</span>
  </div>
  <div class="flex items-center gap-4">
    <a href="tel:+94112345678" class="hidden sm:flex items-center gap-1 hover:opacity-80 transition" style="color:#B7C2DE;"><i class="fas fa-phone"></i> +94 11 234 5678</a>
    <div class="profile-dropdown">
      <span class="profile-trigger flex items-center gap-1 hover:opacity-80 transition" style="color:#B7C2DE;" onclick="toggleDropdown()">
        <i class="fas fa-circle-user"></i> <span class="hidden xs:inline">Account</span> <i class="fas fa-chevron-down" style="font-size:10px;"></i>
      </span>
      <div id="profileDropdown" class="profile-dropdown-content">
        <a href="account.html"><i class="fas fa-user"></i> My Profile</a>
        <a href="orders.html"><i class="fas fa-receipt"></i> Order History</a>
        <a href="watchlist.html"><i class="fas fa-heart"></i> Watchlist</a>
        <div class="divider"></div>
        <a href="login.html" class="logout-item"><i class="fas fa-arrow-right-from-bracket"></i> Logout</a>
      </div>
    </div>
  </div>
</div>

<header class="bg-white border-b px-4 md:px-8 py-3 flex flex-wrap items-center justify-between gap-4" style="border-color:var(--line);">
  <a href="index.html" class="flex items-center gap-2 shrink-0">
    <img src="assets/img/logo.jpeg" alt="ZyroTech" class="h-11 w-11 rounded-xl object-cover">
    <span class="font-display font-bold text-xl tracking-tight" style="color:var(--indigo);">Zyro<span style="color:var(--cyan);">Tech</span></span>
  </a>

  <nav class="hidden lg:flex items-center gap-7 font-display text-sm font-medium" style="color:var(--indigo);">
    <a href="index.html" class="hover-text-red transition">Home</a>
    <a href="index.html#shop" class="hover-text-red transition">Shop</a>
    <a href="index.html#deals" class="hover-text-red transition">Deals</a>
    <a href="index.html#about" class="hover-text-red transition">About</a>
  </nav>

  <div class="flex-1 max-w-md order-3 lg:order-none">
    <div class="relative flex items-center rounded-full border" style="background:var(--indigo-50); border-color:var(--line);">
      <input type="text" placeholder="Search for products..." class="w-full bg-transparent py-2.5 px-4 rounded-full outline-none text-sm">
      <button class="btn-primary !py-2 !px-4 mr-1 !shadow-none">
        <i class="fas fa-search text-xs"></i>
      </button>
    </div>
  </div>

  <div class="flex items-center gap-3 md:gap-5">
    <a href="watchlist.html" class="relative action-icon !w-10 !h-10 text-lg">
      <i class="far fa-heart"></i>
      <span class="badge-dot">3</span>
    </a>
    <a href="cart.html" class="relative action-icon !w-10 !h-10 text-lg">
      <i class="fas fa-cart-shopping"></i>
      <span class="badge-dot">5</span>
    </a>
  </div>
</header>`;
    }
}
customElements.define("header-content",HeaderContent);

function toggleDropdown() {
    const dropdown = document.getElementById('profileDropdown');
    if (dropdown) {
        dropdown.classList.toggle('show');
    }
}

document.addEventListener('click', function(event) {
    const dropdown = document.getElementById('profileDropdown');
    if (dropdown && !event.target.closest('.profile-dropdown')) {
        dropdown.classList.remove('show');
    }
});

document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        const dropdown = document.getElementById('profileDropdown');
        if (dropdown) {
            dropdown.classList.remove('show');
        }
    }
});