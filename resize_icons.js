import sharp from 'sharp';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const publicDir = path.join(__dirname, 'public');

async function resizeImage(filename, size) {
    const filePath = path.join(publicDir, filename);
    try {
        const buffer = await sharp(filePath)
            .resize(size, size)
            .toBuffer();
        await sharp(buffer).toFile(filePath);
        console.log(`Resized ${filename} to ${size}x${size}`);
    } catch (error) {
        console.error(`Error resizing ${filename}:`, error);
    }
}

resizeImage('pwa-192x192.png', 192);
resizeImage('pwa-512x512.png', 512);
