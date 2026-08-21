class FooterContent extends HTMLElement{
    connectedCallback(){
        this.innerHTML = `
<footer style="background:var(--indigo);" class="text-white mt-16">
  <div class="max-w-7xl mx-auto px-4 md:px-8 py-14 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-10">
    <div>
      <div class="flex items-center gap-2 mb-4">
        <img src="assets/img/logo.jpeg" alt="ZyroTech" class="h-10 w-10 rounded-lg object-cover">
        <span class="font-display font-bold text-lg">Zyro<span style="color:var(--cyan);">Tech</span></span>
      </div>
      <p class="text-sm leading-relaxed" style="color:#93A0C4;">Power in every device. Curated electronics for people who expect more from their tech.</p>
      <div class="flex items-center gap-3 mt-5">
        <a href="#" class="action-icon !bg-white/10 !text-white"><i class="fab fa-facebook-f"></i></a>
        <a href="#" class="action-icon !bg-white/10 !text-white"><i class="fab fa-instagram"></i></a>
        <a href="#" class="action-icon !bg-white/10 !text-white"><i class="fab fa-x-twitter"></i></a>
      </div>
    </div>
    <div>
      <h4 class="font-display font-semibold mb-4 text-sm tracking-wide" style="color:var(--cyan);">SHOP</h4>
      <ul class="space-y-2.5 text-sm" style="color:#B7C2DE;">
        <li><a href="index.html" class="hover:text-white transition">New Arrivals</a></li>
        <li><a href="index.html" class="hover:text-white transition">Laptops</a></li>
        <li><a href="index.html" class="hover:text-white transition">Smartphones</a></li>
        <li><a href="index.html" class="hover:text-white transition">Accessories</a></li>
      </ul>
    </div>
    <div>
      <h4 class="font-display font-semibold mb-4 text-sm tracking-wide" style="color:var(--cyan);">SUPPORT</h4>
      <ul class="space-y-2.5 text-sm" style="color:#B7C2DE;">
        <li><a href="cart.html" class="hover:text-white transition">Your Cart</a></li>
        <li><a href="checkout.html" class="hover:text-white transition">Checkout</a></li>
        <li><a href="#" class="hover:text-white transition">Shipping Info</a></li>
        <li><a href="#" class="hover:text-white transition">Returns</a></li>
      </ul>
    </div>
    <div>
      <h4 class="font-display font-semibold mb-4 text-sm tracking-wide" style="color:var(--cyan);">STAY CHARGED</h4>
      <p class="text-sm mb-3" style="color:#B7C2DE;">Deals and new drops, straight to your inbox.</p>
      <div class="flex items-center rounded-full overflow-hidden bg-white/10 border border-white/10">
        <input type="email" placeholder="Email address" class="bg-transparent px-4 py-2.5 text-sm outline-none flex-1 text-white placeholder-white/40">
        <button class="px-4 text-cyan-300"><i class="fas fa-paper-plane"></i></button>
      </div>
    </div>
  </div>
  <div class="border-t border-white/10 py-5 px-4 md:px-8 flex flex-wrap items-center justify-between gap-3 text-xs" style="color:#7A87AC;">
    <span>&copy; 2026 ZyroTech. All rights reserved.</span>
    <span class="flex items-center gap-4"><a href="#" class="hover:text-white transition">Privacy</a><a href="#" class="hover:text-white transition">Terms</a></span>
  </div>
</footer>`;
    }
}
customElements.define("footer-content",FooterContent);
