import { defineConfig } from "tsdown";

export default defineConfig({
  entry: ["src/index.ts"],
  format: ["esm", "cjs", "iife"],
  globalName: "Profat",
  dts: true,
  clean: true,
  outDir: "dist",
});
