import path from "node:path";
import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  reactStrictMode: true,
  // 상위 디렉토리의 다른 lockfile에 영향받지 않도록 이 web 폴더를 추적 루트로 고정합니다.
  outputFileTracingRoot: path.join(__dirname),
};

export default nextConfig;
