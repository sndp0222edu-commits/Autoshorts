/**
 * AntiGem 2D Canvas & Physics Controller
 */

class AntigravityCanvas {
    constructor(canvas) {
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');
        this.width = window.innerWidth;
        this.height = window.innerHeight;
        this.particles = [];
        this.vortices = [];
        this.gravity = 0; // Default zero-gravity
        this.tiltX = 0;
        this.tiltY = 0;
        this.useTilt = true;
        this.isFlyingXKCD = false;
        this.xkcdX = -100;
        this.xkcdY = 200;

        this.touchActive = false;
        this.touchX = 0;
        this.touchY = 0;
        this.touchMode = 'repel'; // 'repel' (antigravity) or 'attract'

        this.colors = ['#00E5FF', '#7C4DFF', '#00E676', '#FF1744', '#FFEA00', '#F50057'];
        this.resize();
        window.addEventListener('resize', () => this.resize());

        this.initTouch();
        this.initDefaultScene();
        this.lastTime = performance.now();
        this.fps = 60;
        this.fpsCounter = document.getElementById('fps-counter');
        this.countCounter = document.getElementById('particle-count');

        this.animate = this.animate.bind(this);
        requestAnimationFrame(this.animate);
    }

    resize() {
        this.width = window.innerWidth;
        this.height = window.innerHeight;
        this.canvas.width = this.width * window.devicePixelRatio;
        this.canvas.height = this.height * window.devicePixelRatio;
        this.ctx.scale(window.devicePixelRatio, window.devicePixelRatio);
    }

    initTouch() {
        const handleStart = (e) => {
            const touch = e.touches ? e.touches[0] : e;
            this.touchActive = true;
            this.touchX = touch.clientX;
            this.touchY = touch.clientY;
            this.vibrate(15);
        };
        const handleMove = (e) => {
            if (!this.touchActive) return;
            const touch = e.touches ? e.touches[0] : e;
            this.touchX = touch.clientX;
            this.touchY = touch.clientY;
        };
        const handleEnd = () => {
            this.touchActive = false;
        };

        this.canvas.addEventListener('touchstart', handleStart, { passive: false });
        this.canvas.addEventListener('touchmove', handleMove, { passive: false });
        this.canvas.addEventListener('touchend', handleEnd);

        this.canvas.addEventListener('mousedown', handleStart);
        window.addEventListener('mousemove', handleMove);
        window.addEventListener('mouseup', handleEnd);

        // Double tap for particle burst
        let lastTap = 0;
        this.canvas.addEventListener('touchend', (e) => {
            const now = Date.now();
            if (now - lastTap < 300) {
                const touch = e.changedTouches ? e.changedTouches[0] : e;
                this.createBurst(touch.clientX, touch.clientY, 35);
                this.vibrate(40);
            }
            lastTap = now;
        });
    }

    vibrate(ms) {
        if (window.AndroidBridge && window.AndroidBridge.vibrate) {
            try { window.AndroidBridge.vibrate(ms); } catch(e){}
        } else if (navigator.vibrate) {
            navigator.vibrate(ms);
        }
    }

    setGravity(g) {
        this.gravity = g;
    }

    setTilt(x, y) {
        this.tiltX = x;
        this.tiltY = y;
    }

    clear() {
        this.particles = [];
        this.vortices = [];
        this.isFlyingXKCD = false;
    }

    spawnParticle(args, scope) {
        const pe = window.engine;
        const x = args.x !== undefined ? pe.evalExpr(args.x, scope) : this.width / 2;
        const y = args.y !== undefined ? pe.evalExpr(args.y, scope) : this.height / 2;
        const vx = args.vx !== undefined ? pe.evalExpr(args.vx, scope) : (Math.random() - 0.5) * 6;
        const vy = args.vy !== undefined ? pe.evalExpr(args.vy, scope) : (Math.random() - 0.5) * 6;
        const radius = args.radius !== undefined ? pe.evalExpr(args.radius, scope) : (Math.random() * 5 + 3);
        const color = args.color ? pe.evalExpr(args.color, scope) : this.colors[Math.floor(Math.random() * this.colors.length)];
        const antigravity = args.antigravity !== undefined ? Boolean(pe.evalExpr(args.antigravity, scope)) : true;

        this.particles.push({
            x: Number(x),
            y: Number(y),
            vx: Number(vx),
            vy: Number(vy),
            radius: Number(radius),
            color: String(color),
            antigravity: antigravity,
            life: 1.0,
            trail: []
        });

        if (this.particles.length > 500) {
            this.particles.shift();
        }
    }

    createBurst(x, y, count = 25) {
        for (let i = 0; i < count; i++) {
            const angle = Math.random() * Math.PI * 2;
            const speed = Math.random() * 8 + 2;
            this.particles.push({
                x: x,
                y: y,
                vx: Math.cos(angle) * speed,
                vy: Math.sin(angle) * speed,
                radius: Math.random() * 4 + 2,
                color: this.colors[Math.floor(Math.random() * this.colors.length)],
                antigravity: true,
                trail: []
            });
        }
    }

    addVortex(x, y, strength) {
        this.vortices.push({ x, y, strength });
    }

    triggerAntigravityFly() {
        this.isFlyingXKCD = true;
        this.xkcdX = -80;
        this.xkcdY = this.height * 0.7;
        this.gravity = -0.3; // float upwards!
        this.createBurst(this.width / 2, this.height / 2, 40);
        this.vibrate(60);
    }

    initDefaultScene() {
        // Spawn initial floating nodes
        for (let i = 0; i < 45; i++) {
            this.particles.push({
                x: Math.random() * this.width,
                y: Math.random() * this.height,
                vx: (Math.random() - 0.5) * 2,
                vy: (Math.random() - 0.5) * 2,
                radius: Math.random() * 5 + 3,
                color: this.colors[i % this.colors.length],
                antigravity: true,
                trail: []
            });
        }
    }

    animate(now) {
        requestAnimationFrame(this.animate);

        const delta = (now - this.lastTime) / 1000;
        this.lastTime = now;
        if (delta > 0) {
            this.fps = Math.round(1 / delta);
            if (this.fpsCounter && Math.random() < 0.1) {
                this.fpsCounter.textContent = `${this.fps} FPS`;
            }
        }
        if (this.countCounter && Math.random() < 0.1) {
            this.countCounter.textContent = `${this.particles.length} ORBS`;
        }

        // Dark background with faint persistence trail
        this.ctx.fillStyle = 'rgba(14, 17, 23, 0.28)';
        this.ctx.fillRect(0, 0, this.width, this.height);

        // Effective gravity calculation
        let gx = 0;
        let gy = this.gravity;
        if (this.useTilt) {
            gx += this.tiltX * 0.15;
            gy += this.tiltY * 0.15;
        }

        // Draw Touch Field
        if (this.touchActive) {
            const gradient = this.ctx.createRadialGradient(
                this.touchX, this.touchY, 5,
                this.touchX, this.touchY, 120
            );
            gradient.addColorStop(0, this.touchMode === 'repel' ? 'rgba(0, 229, 255, 0.4)' : 'rgba(255, 23, 68, 0.4)');
            gradient.addColorStop(1, 'transparent');
            this.ctx.fillStyle = gradient;
            this.ctx.beginPath();
            this.ctx.arc(this.touchX, this.touchY, 120, 0, Math.PI * 2);
            this.ctx.fill();
        }

        // Update & Render Particles
        for (let i = 0; i < this.particles.length; i++) {
            const p = this.particles[i];

            // Apply gravity
            p.vx += gx;
            p.vy += gy;

            // Touch interaction (Antigravity repulsion or attraction)
            if (this.touchActive) {
                const dx = p.x - this.touchX;
                const dy = p.y - this.touchY;
                const dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < 180 && dist > 1) {
                    const force = (180 - dist) / 180;
                    const factor = this.touchMode === 'repel' ? 1.8 : -1.8;
                    p.vx += (dx / dist) * force * factor;
                    p.vy += (dy / dist) * force * factor;
                }
            }

            // Vortices
            for (let v of this.vortices) {
                const dx = v.x - p.x;
                const dy = v.y - p.y;
                const dist = Math.sqrt(dx * dx + dy * dy);
                if (dist > 5) {
                    p.vx += (-dy / dist) * v.strength;
                    p.vy += (dx / dist) * v.strength;
                }
            }

            // Air friction
            p.vx *= 0.99;
            p.vy *= 0.99;

            p.x += p.vx;
            p.y += p.vy;

            // Boundary Bounce
            if (p.x < p.radius) {
                p.x = p.radius;
                p.vx = -p.vx * 0.85;
            } else if (p.x > this.width - p.radius) {
                p.x = this.width - p.radius;
                p.vx = -p.vx * 0.85;
            }
            if (p.y < p.radius) {
                p.y = p.radius;
                p.vy = -p.vy * 0.85;
            } else if (p.y > this.height - p.radius) {
                p.y = this.height - p.radius;
                p.vy = -p.vy * 0.85;
            }

            // Draw Neon Particle
            this.ctx.shadowBlur = 12;
            this.ctx.shadowColor = p.color;
            this.ctx.fillStyle = p.color;
            this.ctx.beginPath();
            this.ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2);
            this.ctx.fill();
        }
        this.ctx.shadowBlur = 0;

        // Render Connections for aesthetic 2D network (Antigravity web)
        for (let i = 0; i < Math.min(this.particles.length, 60); i++) {
            for (let j = i + 1; j < Math.min(this.particles.length, 60); j++) {
                const p1 = this.particles[i];
                const p2 = this.particles[j];
                const dx = p1.x - p2.x;
                const dy = p1.y - p2.y;
                const dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < 85) {
                    this.ctx.strokeStyle = `rgba(0, 229, 255, ${1 - dist / 85 * 0.8})`;
                    this.ctx.lineWidth = 0.8;
                    this.ctx.beginPath();
                    this.ctx.moveTo(p1.x, p1.y);
                    this.ctx.lineTo(p2.x, p2.y);
                    this.ctx.stroke();
                }
            }
        }

        // Draw XKCD Antigravity Flying Character
        if (this.isFlyingXKCD) {
            this.xkcdX += 2.5;
            this.xkcdY -= 1.2;

            this.drawXKCDFlyer(this.xkcdX, this.xkcdY);

            if (this.xkcdX > this.width + 100 || this.xkcdY < -100) {
                this.isFlyingXKCD = false;
            }
        }
    }

    drawXKCDFlyer(x, y) {
        this.ctx.save();
        this.ctx.strokeStyle = '#FFFFFF';
        this.ctx.lineWidth = 2.5;
        this.ctx.fillStyle = '#FFFFFF';

        // Head
        this.ctx.beginPath();
        this.ctx.arc(x, y, 10, 0, Math.PI * 2);
        this.ctx.stroke();

        // Body
        this.ctx.beginPath();
        this.ctx.moveTo(x, y + 10);
        this.ctx.lineTo(x, y + 35);
        // Arms spread (flying!)
        this.ctx.moveTo(x - 20, y + 20);
        this.ctx.lineTo(x + 20, y + 20);
        // Legs trailing
        this.ctx.moveTo(x, y + 35);
        this.ctx.lineTo(x - 15, y + 55);
        this.ctx.moveTo(x, y + 35);
        this.ctx.lineTo(x + 15, y + 55);
        this.ctx.stroke();

        // Speech bubble: "Python antigravity!"
        this.ctx.fillStyle = 'rgba(0, 229, 255, 0.9)';
        this.ctx.font = '12px monospace';
        this.ctx.fillText("import antigravity!", x + 15, y - 5);
        this.ctx.restore();
    }
}

// Global hook for Android sensor tilt
window.onDeviceTilt = function(x, y) {
    if (window.canvasController) {
        window.canvasController.setTilt(x, y);
    }
};

window.AntigravityCanvas = AntigravityCanvas;
