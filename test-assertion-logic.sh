#!/bin/bash

# Quick test to validate Google search assertion logic
# This mimics what the test assertion should encounter

echo "🧪 Testing Google Search Assertion Logic"
echo "=========================================="

# Test case 1: Typical Google search results page
echo "Test 1: Typical Google search results"
title1="Artificial Intelligence Testing - Google Search"
url1="https://www.google.com/search?q=artificial+intelligence+testing"

if [[ "${title1,,}" == *"google"* ]] && [[ "${url1,,}" == *"google"* ]] && [[ "${url1,,}" == *"search"* ]]; then
    echo "✅ PASS: Typical search results page detected correctly"
else
    echo "❌ FAIL: Should have detected typical search results page"
fi

# Test case 2: Google homepage (fallback scenario)
echo "Test 2: Google homepage (fallback)"
title2="Google"
url2="https://www.google.com/"

if [[ "${url2,,}" == *"google.com"* ]]; then
    echo "✅ PASS: Google domain fallback working"
else
    echo "❌ FAIL: Should have detected Google domain"
fi

# Test case 3: Non-Google page (should fail)
echo "Test 3: Non-Google page (should fail)"
title3="Wikipedia"
url3="https://www.wikipedia.org/"

if [[ "${title3,,}" == *"google"* ]] || [[ "${url3,,}" == *"google.com"* ]]; then
    echo "❌ FAIL: Should not have detected non-Google page as valid"
else
    echo "✅ PASS: Correctly rejected non-Google page"
fi

echo ""
echo "🎯 Assertion Logic Validation Complete"
echo "The new assertion logic should be much more reliable!"