package fi.nls.oskari.printout.caching;

public interface BlobCache {
	public byte[] getFromCache(byte[] key);

	public void putToCache(byte[] key, byte[] blob);
}
