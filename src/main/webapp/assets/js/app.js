(function () {
  const drawer = document.querySelector("[data-drawer]");
  function setDrawer(open) {
    drawer?.classList.toggle("open", open);
    document.body.classList.toggle("drawer-open", open);
  }
  document
    .querySelectorAll("[data-drawer-open]")
    .forEach((btn) => btn.addEventListener("click", () => setDrawer(true)));
  document
    .querySelectorAll("[data-drawer-close]")
    .forEach((btn) => btn.addEventListener("click", () => setDrawer(false)));
  document.addEventListener("keydown", (event) => {
    if (event.key === "Escape") setDrawer(false);
  });

  document.querySelectorAll("[data-promo-close]").forEach((btn) =>
    btn.addEventListener("click", () => {
      const promo = btn.closest("[data-promo]");
      promo?.classList.add("is-hidden");
      try {
        sessionStorage.setItem("watchstore-promo-hidden", "1");
      } catch (ignored) {
        /* Storage can be unavailable. */
      }
    }),
  );
  try {
    if (sessionStorage.getItem("watchstore-promo-hidden") === "1")
      document.querySelector("[data-promo]")?.classList.add("is-hidden");
  } catch (ignored) {
    /* The promotion remains visible when storage is unavailable. */
  }

  const filter = document.querySelector("[data-filter]");
  document
    .querySelectorAll("[data-filter-toggle]")
    .forEach((btn) =>
      btn.addEventListener("click", () => filter?.classList.toggle("open")),
    );

  const sidebar = document.querySelector("[data-sidebar]");
  document
    .querySelectorAll("[data-sidebar-toggle]")
    .forEach((btn) =>
      btn.addEventListener("click", () => sidebar?.classList.toggle("open")),
    );

  document.querySelectorAll("[data-hero-carousel]").forEach((carousel) => {
    const slides = Array.from(carousel.querySelectorAll(".hero-slide"));
    const dotsRoot = carousel.querySelector("[data-hero-dots]");
    const counter = carousel.querySelector("[data-hero-counter]");
    const previous = carousel.querySelector("[data-hero-prev]");
    const next = carousel.querySelector("[data-hero-next]");
    const interval = Number(carousel.dataset.autoplay) || 5500;
    const reducedMotion = window.matchMedia(
      "(prefers-reduced-motion: reduce)",
    ).matches;
    let current = 0;
    let timer;
    let touchStartX = 0;

    const dots = slides.map((slide, index) => {
      const dot = document.createElement("button");
      dot.type = "button";
      dot.className = "hero-dot";
      dot.setAttribute("role", "tab");
      dot.setAttribute("aria-label", "Hiển thị banner " + (index + 1));
      dot.addEventListener("click", () => show(index));
      dotsRoot?.appendChild(dot);
      return dot;
    });

    function start() {
      clearInterval(timer);
      if (!reducedMotion && !document.hidden && slides.length > 1) {
        timer = window.setInterval(() => show(current + 1, false), interval);
      }
    }

    function show(position, restart = true) {
      current = (position + slides.length) % slides.length;
      slides.forEach((slide, index) => {
        const active = index === current;
        slide.classList.toggle("active", active);
        slide.setAttribute("aria-hidden", String(!active));
        slide.toggleAttribute("inert", !active);
      });
      dots.forEach((dot, index) => {
        const active = index === current;
        dot.classList.toggle("active", active);
        dot.setAttribute("aria-selected", String(active));
      });
      if (counter)
        counter.textContent =
          String(current + 1).padStart(2, "0") +
          " / " +
          String(slides.length).padStart(2, "0");
      if (restart) start();
    }

    previous?.addEventListener("click", () => show(current - 1));
    next?.addEventListener("click", () => show(current + 1));
    carousel.addEventListener("mouseenter", () => clearInterval(timer));
    carousel.addEventListener("mouseleave", start);
    carousel.addEventListener("focusin", () => clearInterval(timer));
    carousel.addEventListener("focusout", (event) => {
      if (!carousel.contains(event.relatedTarget)) start();
    });
    carousel.addEventListener("keydown", (event) => {
      if (event.key === "ArrowLeft") show(current - 1);
      if (event.key === "ArrowRight") show(current + 1);
    });
    carousel.addEventListener(
      "touchstart",
      (event) => {
        touchStartX = event.changedTouches[0].clientX;
      },
      { passive: true },
    );
    carousel.addEventListener(
      "touchend",
      (event) => {
        const distance = event.changedTouches[0].clientX - touchStartX;
        if (Math.abs(distance) > 55) show(current + (distance < 0 ? 1 : -1));
      },
      { passive: true },
    );
    document.addEventListener("visibilitychange", () =>
      document.hidden ? clearInterval(timer) : start(),
    );

    show(0);
  });

  document
    .querySelectorAll("[data-campaign-countdown]")
    .forEach((countdown) => {
      const storageKey = "watchstore-campaign-deadline";
      const duration = (Number(countdown.dataset.hours) || 47) * 60 * 60 * 1000;
      let deadline = Date.now() + duration;
      try {
        const saved = Number(localStorage.getItem(storageKey));
        if (saved > Date.now()) deadline = saved;
        else localStorage.setItem(storageKey, String(deadline));
      } catch (ignored) {
        /* A session-only countdown is an acceptable fallback. */
      }

      const fields = {
        days: countdown.querySelector("[data-days]"),
        hours: countdown.querySelector("[data-hours]"),
        minutes: countdown.querySelector("[data-minutes]"),
        seconds: countdown.querySelector("[data-seconds]"),
      };
      function renderCountdown() {
        let remaining = Math.max(0, deadline - Date.now());
        const days = Math.floor(remaining / 86400000);
        remaining %= 86400000;
        const hours = Math.floor(remaining / 3600000);
        remaining %= 3600000;
        const minutes = Math.floor(remaining / 60000);
        remaining %= 60000;
        const seconds = Math.floor(remaining / 1000);
        fields.days &&
          (fields.days.textContent = String(days).padStart(2, "0"));
        fields.hours &&
          (fields.hours.textContent = String(hours).padStart(2, "0"));
        fields.minutes &&
          (fields.minutes.textContent = String(minutes).padStart(2, "0"));
        fields.seconds &&
          (fields.seconds.textContent = String(seconds).padStart(2, "0"));
      }
      renderCountdown();
      window.setInterval(renderCountdown, 1000);
    });

  document.querySelectorAll("[data-quantity-minus]").forEach((btn) =>
    btn.addEventListener("click", () => {
      const input = btn.parentElement.querySelector('input[type="number"]');
      input.value = Math.max(1, Number(input.value || 1) - 1);
    }),
  );
  document.querySelectorAll("[data-quantity-plus]").forEach((btn) =>
    btn.addEventListener("click", () => {
      const input = btn.parentElement.querySelector('input[type="number"]');
      input.value = Number(input.value || 1) + 1;
    }),
  );

  function toast(message) {
    let node = document.querySelector(".toast.dynamic");
    if (!node) {
      node = document.createElement("div");
      node.className = "toast dynamic show";
      document.body.appendChild(node);
    }
    node.textContent = "✓ " + message;
    clearTimeout(window.watchStoreToast);
    window.watchStoreToast = setTimeout(() => node.remove(), 2200);
  }
  document.querySelectorAll("[data-demo-toast]").forEach((btn) =>
    btn.addEventListener("click", (event) => {
      if (btn.type !== "submit") event.preventDefault();
      toast(btn.dataset.demoToast);
    }),
  );
  document.querySelectorAll("[data-copy]").forEach((btn) =>
    btn.addEventListener("click", () => {
      navigator.clipboard?.writeText(btn.dataset.copy);
      toast("Đã sao chép mã " + btn.dataset.copy);
    }),
  );
  setTimeout(
    () => document.querySelector(".toast.show:not(.dynamic)")?.remove(),
    2600,
  );
})();
