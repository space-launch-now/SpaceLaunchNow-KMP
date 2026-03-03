// CacheKeyTests.swift
// LaunchWidgetTests
//
// Tests for the SHA-256 cache key algorithm used in LaunchData.swift cacheFile(for:).
// These tests validate the contract: the cache key function MUST produce unique,
// deterministic, 64-character lowercase hex filenames from URL strings.
//
// The hash function is replicated here because widget extensions (app extensions)
// cannot be @testable imported. The algorithm under test is the SHA-256 hash of the
// full URL, matching the implementation in LaunchWidget/LaunchData.swift.

import CryptoKit
import Foundation
import XCTest

final class CacheKeyTests: XCTestCase {

    // MARK: - Helper: Same algorithm as LaunchProvider.cacheFile(for:)

    /// Computes the SHA-256 cache key hash for a URL string.
    /// This MUST match the implementation in LaunchWidget/LaunchData.swift.
    private func cacheKeyHash(for urlString: String) -> String {
        let data = Data(urlString.utf8)
        let hash = SHA256.hash(data: data)
        return hash.compactMap { String(format: "%02x", $0) }.joined()
    }

    /// Returns the full cache filename (hash + .jpg extension).
    private func cacheFileName(for urlString: String) -> String {
        return cacheKeyHash(for: urlString) + ".jpg"
    }

    // MARK: - User Story 1: Correct Image per Launch (T005–T007)

    /// T005: Two different CDN URLs MUST produce different cache keys.
    /// This is the core regression test for the cache collision bug.
    func testDistinctURLsProduceDistinctCacheKeys() {
        let url1 = "https://spacelaunchnow-prod-east.nyc3.cdn.digitaloceanspaces.com/media/launch_images/falcon925/image1.jpg"
        let url2 = "https://spacelaunchnow-prod-east.nyc3.cdn.digitaloceanspaces.com/media/launch_images/falcon926/image2.jpg"

        let key1 = cacheKeyHash(for: url1)
        let key2 = cacheKeyHash(for: url2)

        XCTAssertNotEqual(key1, key2, "Different URLs must produce different cache keys")
    }

    /// T006: The same URL called twice MUST always produce the same cache key (deterministic).
    func testSameURLProducesSameCacheKey() {
        let url = "https://spacelaunchnow-prod-east.nyc3.cdn.digitaloceanspaces.com/media/launch_images/falcon9/starlink.jpg"

        let key1 = cacheKeyHash(for: url)
        let key2 = cacheKeyHash(for: url)

        XCTAssertEqual(key1, key2, "Same URL must always produce the same cache key")
    }

    /// T007: URLs differing only in the last path segment MUST produce different keys.
    /// Regression test: the old base64+prefix(64) algorithm truncated URLs to ~48 bytes,
    /// so same-domain URLs with different paths produced identical keys.
    func testCacheKeyUsesFullURLNotPrefix() {
        let baseURL = "https://spacelaunchnow-prod-east.nyc3.cdn.digitaloceanspaces.com/media/launch_images/"
        let url1 = baseURL + "atlasv551/launch_photo_a.jpg"
        let url2 = baseURL + "atlasv551/launch_photo_b.jpg"

        let key1 = cacheKeyHash(for: url1)
        let key2 = cacheKeyHash(for: url2)

        XCTAssertNotEqual(key1, key2, "URLs differing only in filename must produce different cache keys (regression: base64 prefix truncation)")
    }

    // MARK: - User Story 2: Cache Reliability (T010–T012)

    /// T010: The hash output MUST be exactly 64 lowercase hexadecimal characters.
    /// SHA-256 produces 256 bits = 32 bytes = 64 hex chars.
    func testCacheKeyOutputIs64CharHex() {
        let url = "https://example.com/image.jpg"
        let key = cacheKeyHash(for: url)

        XCTAssertEqual(key.count, 64, "SHA-256 hash must produce exactly 64 hex characters")

        let hexCharacterSet = CharacterSet(charactersIn: "0123456789abcdef")
        let isAllHex = key.unicodeScalars.allSatisfy { hexCharacterSet.contains($0) }
        XCTAssertTrue(isAllHex, "Cache key must contain only lowercase hexadecimal characters")
    }

    /// T011: An empty URL string MUST produce a valid (non-crashing) cache file path.
    func testCacheKeyHandlesEmptyString() {
        let key = cacheKeyHash(for: "")

        XCTAssertEqual(key.count, 64, "Empty string should still produce a 64-char hash")
        XCTAssertFalse(key.isEmpty, "Empty input must not produce empty output")

        let fileName = cacheFileName(for: "")
        XCTAssertTrue(fileName.hasSuffix(".jpg"), "Cache filename must end with .jpg")
    }

    /// T012: URLs with query parameters, fragments, and unicode MUST produce valid filenames.
    func testCacheKeyHandlesSpecialCharacters() {
        let urlWithQuery = "https://cdn.example.com/image.jpg?width=200&height=200"
        let urlWithFragment = "https://cdn.example.com/image.jpg#section"
        let urlWithUnicode = "https://cdn.example.com/ロケット/image.jpg"
        let urlWithSpaces = "https://cdn.example.com/my%20image.jpg"

        // All should produce valid 64-char hex keys without crashing
        let keys = [urlWithQuery, urlWithFragment, urlWithUnicode, urlWithSpaces].map { cacheKeyHash(for: $0) }

        for (index, key) in keys.enumerated() {
            XCTAssertEqual(key.count, 64, "URL at index \(index) should produce 64-char hash")
        }

        // All different URLs should produce different keys
        let uniqueKeys = Set(keys)
        XCTAssertEqual(uniqueKeys.count, keys.count, "All distinct URLs must produce distinct cache keys")
    }
}
