from PIL import Image
import os

def resize_image(path, size):
    try:
        img = Image.open(path)
        img = img.resize(size, Image.Resampling.LANCZOS)
        img.save(path)
        print(f"Resized {path} to {size}")
    except Exception as e:
        print(f"Error resizing {path}: {e}")

public_dir = '/home/fk/.gemini/antigravity/scratch/med-tracker/public'
resize_image(os.path.join(public_dir, 'pwa-192x192.png'), (192, 192))
resize_image(os.path.join(public_dir, 'pwa-512x512.png'), (512, 512))
