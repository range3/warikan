# Regenerates app/src/main/res/drawable-nodpi/ic_launcher_foreground.png from app_icon_source.jpg:
#   powershell -File art/icon/make_icon.ps1 -Src art/icon/app_icon_source.jpg -Out app/src/main/res/drawable-nodpi/ic_launcher_foreground.png
# Cuts the coins out of the solid background (printed as bg=#RRGGBB; keep values/ic_launcher_background.xml in sync)
# and centers them at 216px wide in a 432px (108dp @ xxxhdpi) adaptive-icon layer, inside the safe zone.

param([string]$Src, [string]$Out, [int]$Size = 432, [int]$TargetWidth = 216)

Add-Type -ReferencedAssemblies System.Drawing -TypeDefinition @'
using System;
using System.Drawing;
using System.Drawing.Imaging;
using System.Drawing.Drawing2D;

public static class IconMaker {
    public static string Run(string src, string outPath, int size, int targetWidth) {
        using (var bmp = new Bitmap(src)) {
            int w = bmp.Width, h = bmp.Height;
            // background = mean of 8px border
            long sr = 0, sg = 0, sb = 0, n = 0;
            for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
                if (x >= 8 && x < w - 8 && y >= 8 && y < h - 8) continue;
                var c = bmp.GetPixel(x, y); sr += c.R; sg += c.G; sb += c.B; n++;
            }
            int br = (int)(sr / n), bg = (int)(sg / n), bb = (int)(sb / n);

            // alpha from distance to bg color; un-mix bg from edges
            var cut = new Bitmap(w, h, PixelFormat.Format32bppArgb);
            int minX = w, minY = h, maxX = 0, maxY = 0;
            const double t0 = 40, t1 = 110;
            for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
                var c = bmp.GetPixel(x, y);
                double d = Math.Sqrt(Math.Pow(c.R - br, 2) + Math.Pow(c.G - bg, 2) + Math.Pow(c.B - bb, 2));
                double a = Math.Max(0, Math.Min(1, (d - t0) / (t1 - t0)));
                if (a <= 0) { cut.SetPixel(x, y, Color.Transparent); continue; }
                Func<int, int, int> un = (p, q) => (int)Math.Max(0, Math.Min(255, (p - (1 - a) * q) / a));
                cut.SetPixel(x, y, Color.FromArgb((int)(a * 255), un(c.R, br), un(c.G, bg), un(c.B, bb)));
                if (a > 0.5) { minX = Math.Min(minX, x); minY = Math.Min(minY, y); maxX = Math.Max(maxX, x); maxY = Math.Max(maxY, y); }
            }

            int bw = maxX - minX + 1, bh = maxY - minY + 1;
            double scale = (double)targetWidth / bw;
            int dw = (int)Math.Round(bw * scale), dh = (int)Math.Round(bh * scale);
            using (var outBmp = new Bitmap(size, size, PixelFormat.Format32bppArgb))
            using (var g = Graphics.FromImage(outBmp)) {
                g.Clear(Color.Transparent);
                g.InterpolationMode = InterpolationMode.HighQualityBicubic;
                g.PixelOffsetMode = PixelOffsetMode.HighQuality;
                g.CompositingMode = CompositingMode.SourceOver;
                // pad source rect to avoid bicubic edge clipping
                int pad = 4;
                var srcRect = new Rectangle(minX - pad, minY - pad, bw + 2 * pad, bh + 2 * pad);
                var dstRect = new RectangleF((size - dw) / 2f - (float)(pad * scale), (size - dh) / 2f - (float)(pad * scale),
                                             (float)((bw + 2 * pad) * scale), (float)((bh + 2 * pad) * scale));
                g.DrawImage(cut, dstRect, srcRect, GraphicsUnit.Pixel);
                outBmp.Save(outPath, ImageFormat.Png);
            }
            cut.Dispose();
            return string.Format("bg=#{0:X2}{1:X2}{2:X2} bbox=({3},{4})-({5},{6}) {7}x{8} -> {9}x{10}",
                br, bg, bb, minX, minY, maxX, maxY, bw, bh, dw, dh);
        }
    }
}
'@

[IconMaker]::Run($Src, $Out, $Size, $TargetWidth)
