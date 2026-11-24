/**
 * AntiGem Lightweight Python & Antigravity Runtime Engine
 * Executes Python 2D animation and physics scripts offline in real-time.
 */

class PythonEngine {
    constructor(canvasController) {
        this.controller = canvasController;
        this.outputCallback = null;
    }

    setOutputCallback(cb) {
        this.outputCallback = cb;
    }

    log(msg) {
        if (this.outputCallback) {
            this.outputCallback(msg);
        } else {
            console.log("[Python]", msg);
        }
    }

    run(code) {
        this.log(">>> Running Python script...");
        try {
            const cleanCode = code.trim();

            // Built-in Python 'import antigravity'
            if (cleanCode.includes("import antigravity")) {
                this.log("[import antigravity] XKCD Flying mode activated! Gravity inverted.");
                this.controller.triggerAntigravityFly();
            }

            // Transpile standard python-like commands to canvas controller calls
            const lines = cleanCode.split('\n');
            let executedCount = 0;

            for (let i = 0; i < lines.length; i++) {
                let line = lines[i].trim();
                if (!line || line.startsWith('#')) continue;

                // Handle loops: for i in range(N):
                let loopMatch = line.match(/^for\s+(\w+)\s+in\s+range\(([^)]+)\):/);
                if (loopMatch) {
                    let count = parseInt(this.evalExpr(loopMatch[2])) || 1;
                    // Gather nested block
                    let block = [];
                    while (i + 1 < lines.length && (lines[i + 1].startsWith('    ') || lines[i + 1].startsWith('\t'))) {
                        i++;
                        block.push(lines[i].trim());
                    }
                    for (let c = 0; c < count; c++) {
                        for (let bl of block) {
                            this.executeLine(bl, { [loopMatch[1]]: c });
                        }
                    }
                    executedCount += count;
                    continue;
                }

                this.executeLine(line, {});
                executedCount++;
            }

            this.log(`✓ Python script finished (${executedCount} operations executed).`);
            return { success: true, message: `Executed ${executedCount} operations` };
        } catch (err) {
            this.log(`❌ Python SyntaxError / RuntimeError: ${err.message}`);
            return { success: false, error: err.message };
        }
    }

    executeLine(line, scope = {}) {
        if (!line || line.startsWith('#')) return;

        // Command: spawn(...) or create_orb(...)
        if (line.startsWith('spawn(') || line.startsWith('create_orb(')) {
            const args = this.parseArgs(line);
            this.controller.spawnParticle(args, scope);
            return;
        }

        // Command: set_gravity(g)
        if (line.startsWith('set_gravity(')) {
            const args = this.parseArgs(line);
            const g = parseFloat(args.g ?? args[0] ?? 0);
            this.controller.setGravity(g);
            this.log(`Gravity set to: ${g}`);
            return;
        }

        // Command: burst(x, y, count)
        if (line.startsWith('burst(')) {
            const args = this.parseArgs(line);
            const x = this.evalExpr(args.x ?? args[0] ?? "center_x", scope);
            const y = this.evalExpr(args.y ?? args[1] ?? "center_y", scope);
            const count = parseInt(this.evalExpr(args.count ?? args[2] ?? "20", scope));
            this.controller.createBurst(x, y, count);
            return;
        }

        // Command: clear()
        if (line.startsWith('clear()')) {
            this.controller.clear();
            return;
        }

        // Command: create_vortex(x, y, strength)
        if (line.startsWith('create_vortex(')) {
            const args = this.parseArgs(line);
            const x = this.evalExpr(args.x ?? args[0] ?? "center_x", scope);
            const y = this.evalExpr(args.y ?? args[1] ?? "center_y", scope);
            const strength = parseFloat(args.strength ?? args[2] ?? 1.5);
            this.controller.addVortex(x, y, strength);
            return;
        }

        // Command: print(...)
        if (line.startsWith('print(')) {
            const inner = line.slice(6, -1);
            this.log(this.evalExpr(inner, scope));
            return;
        }
    }

    evalExpr(expr, scope = {}) {
        if (expr === undefined) return 0;
        let s = String(expr).trim();

        // Scope substitutions
        for (let [k, v] of Object.entries(scope)) {
            let re = new RegExp(`\\b${k}\\b`, 'g');
            s = s.replace(re, v);
        }

        s = s.replace(/\bcenter_x\b/g, this.controller.width / 2);
        s = s.replace(/\bcenter_y\b/g, this.controller.height / 2);
        s = s.replace(/\bwidth\b/g, this.controller.width);
        s = s.replace(/\bheight\b/g, this.controller.height);
        s = s.replace(/random\.uniform\(([^,]+),\s*([^)]+)\)/g, '($1 + Math.random() * (($2) - ($1)))');
        s = s.replace(/random\.random\(\)/g, 'Math.random()');
        s = s.replace(/math\.sin\(/g, 'Math.sin(');
        s = s.replace(/math\.cos\(/g, 'Math.cos(');
        s = s.replace(/math\.pi\b/g, 'Math.PI');

        try {
            return Function(`'use strict'; return (${s})`)();
        } catch {
            return s.replace(/['"]/g, '');
        }
    }

    parseArgs(callStr) {
        const open = callStr.indexOf('(');
        const close = callStr.lastIndexOf(')');
        if (open === -1 || close === -1) return {};
        const content = callStr.substring(open + 1, close);
        const parts = this.splitArgs(content);
        const result = {};

        parts.forEach((p, idx) => {
            const eqIdx = p.indexOf('=');
            if (eqIdx !== -1) {
                const key = p.substring(0, eqIdx).trim();
                const val = p.substring(eqIdx + 1).trim();
                result[key] = val;
            } else {
                result[idx] = p.trim();
            }
        });
        return result;
    }

    splitArgs(str) {
        let res = [];
        let cur = '';
        let depth = 0;
        for (let ch of str) {
            if (ch === '(' || ch === '[' || ch === '{') depth++;
            else if (ch === ')' || ch === ']' || ch === '}') depth--;
            if (ch === ',' && depth === 0) {
                res.push(cur);
                cur = '';
            } else {
                cur += ch;
            }
        }
        if (cur.trim()) res.push(cur);
        return res;
    }
}

window.PythonEngine = PythonEngine;
