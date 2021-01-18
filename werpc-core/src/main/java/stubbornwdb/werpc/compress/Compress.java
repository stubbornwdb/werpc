package stubbornwdb.werpc.compress;


import stubbornwdb.werpc.extension.SPI;


@SPI
public interface Compress {

    byte[] compress(byte[] bytes);

    byte[] decompress(byte[] bytes);
}
