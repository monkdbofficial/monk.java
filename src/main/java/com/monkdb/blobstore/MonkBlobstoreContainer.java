// Copyright 2025, Movibase Platform Private Limited
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.monkdb.blobstore;

import com.monkdb.exceptions.MonkBlobLocationNotFoundException;
import com.monkdb.exceptions.MonkDigestNotFoundException;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/**
 * A container for storing and managing blobs (binary large objects) in a blob store.
 * Provides methods for uploading, retrieving, deleting, and checking the existence of blobs.
 */
public class MonkBlobstoreContainer {
    /**
     * The name of this blob container.
     */
    private final String containerName;

    /**
     * The client used to communicate with the blob store service.
     */
    private final MonkClientLike connection;

    /**
     * Constructs a new blob container with the specified name and client connection.
     *
     * @param containerName the name of the container
     * @param connection    the client connection to the blob store service
     */
    public MonkBlobstoreContainer(String containerName, MonkClientLike connection) {
        this.containerName = containerName;
        this.connection = connection;
    }

    /**
     * Computes the SHA-1 digest of the contents of the given input stream asynchronously.
     *
     * @param stream the input stream to compute the digest from
     * @return a CompletableFuture that completes with the hex-encoded digest
     */
    private CompletableFuture<String> computeDigest(InputStream stream) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                byte[] buffer = new byte[8192];
                int read;
                while ((read = stream.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
                stream.close();
                return bytesToHex(digest.digest());
            } catch (IOException | NoSuchAlgorithmException e) {
                throw new CompletionException("Failed to compute digest", e);
            }
        });
    }

    /**
     * Converts a byte array to a hex string.
     *
     * @param bytes the byte array to convert
     * @return the hex string representation of the byte array
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Uploads a blob to the container and returns its digest if successful.
     * The digest is computed automatically from the blob contents.
     *
     * @param file the blob to upload, as a byte array or InputStream
     * @return a CompletableFuture that completes with the digest of the uploaded blob on success,
     * or fails if the upload fails
     * @throws IllegalArgumentException if the file is not a supported type
     */
    public CompletableFuture<String> put(Object file) {
        try {
            InputStream stream;
            ByteArrayOutputStream baos = null;
            if (file instanceof byte[]) {
                stream = new ByteArrayInputStream((byte[]) file);
            } else if (file instanceof InputStream) {
                baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int read;
                InputStream inputStream = (InputStream) file;
                while ((read = inputStream.read(buffer)) != -1) {
                    baos.write(buffer, 0, read);
                }
                stream = new ByteArrayInputStream(baos.toByteArray());
            } else {
                throw new IllegalArgumentException("Unsupported file type: " + file.getClass().getName());
            }
            InputStream finalStream = stream;
            byte[] finalBuffer = baos != null ? baos.toByteArray() : ((byte[]) file);
            return computeDigest(new ByteArrayInputStream(finalBuffer))
                    .thenCompose(digest -> {
                        Map<String, String> headers = Map.of("Content-Type", "application/octet-stream");
                        String path = "/_blobs/" + containerName + "/" + digest;
                        return connection.request("PUT", path, finalStream, headers)
                                .thenApply(response -> {
                                    if (response.status() != 201) {
                                        throw new CompletionException(new RuntimeException("Upload failed"));
                                    }
                                    return digest;
                                });
                    });
        } catch (IOException e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Uploads a blob to the container with a provided digest and returns whether the upload succeeded.
     *
     * @param file   the blob to upload, as a byte array or InputStream
     * @param digest the digest of the blob
     * @return a CompletableFuture that completes with true if the upload succeeded, false otherwise
     * @throws IllegalArgumentException if the file is not a supported type
     */
    public CompletableFuture<Boolean> put(Object file, String digest) {
        try {
            InputStream stream;
            if (file instanceof byte[]) {
                stream = new ByteArrayInputStream((byte[]) file);
            } else if (file instanceof InputStream) {
                stream = (InputStream) file;
            } else {
                throw new IllegalArgumentException("Unsupported file type: " + file.getClass().getName());
            }
            Map<String, String> headers = Map.of("Content-Type", "application/octet-stream");
            String path = "/_blobs/" + containerName + "/" + digest;
            return connection.request("PUT", path, stream, headers)
                    .thenApply(response -> response.status() == 201);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Retrieves a blob by its digest.
     *
     * @param digest the digest of the blob to retrieve
     * @return a CompletableFuture that completes with an InputStream for the blob content,
     * or fails if the blob does not exist or the response is invalid
     * @throws MonkDigestNotFoundException if the blob with the specified digest does not exist
     */
    public CompletableFuture<InputStream> get(String digest) {
        String path = "/_blobs/" + containerName + "/" + digest;
        return connection.request("GET", path, null, null)
                .thenApply(response -> {
                    if (response == null || response.status() == 404) {
                        throw new CompletionException(new MonkDigestNotFoundException(containerName, digest));
                    }
                    if (response.body() == null) {
                        throw new CompletionException(new RuntimeException("Blob response missing stream"));
                    }
                    return response.body();
                });
    }

    /**
     * Deletes a blob by its digest.
     *
     * @param digest the digest of the blob to delete
     * @return a CompletableFuture that completes with true if the blob was deleted,
     * false if the blob does not exist, or fails if an error occurs
     * @throws MonkBlobLocationNotFoundException if the blob could not be deleted for reasons other than not existing
     */
    public CompletableFuture<Boolean> delete(String digest) {
        String path = "/_blobs/" + containerName + "/" + digest;
        return connection.request("DELETE", path, null, null)
                .thenApply(response -> {
                    if (response == null) return false;
                    if (response.status() == 204) return true;
                    if (response.status() == 404) return false;
                    throw new CompletionException(new MonkBlobLocationNotFoundException(containerName, digest));
                });
    }

    /**
     * Checks if a blob with the specified digest exists in the container.
     *
     * @param digest the digest of the blob to check
     * @return a CompletableFuture that completes with true if the blob exists, false otherwise
     */
    public CompletableFuture<Boolean> exists(String digest) {
        String path = "/_blobs/" + containerName + "/" + digest;
        return connection.request("HEAD", path, null, null)
                .thenApply(response -> response != null && response.status() == 200);
    }

    /**
     * Returns a string representation of the blob container.
     *
     * @return a string representation of the blob container
     */
    @Override
    public String toString() {
        return "<BlobContainer '" + containerName + "'>";
    }
}