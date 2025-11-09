#!/bin/bash
# Script to prepare Google Play Store release notes
# Removes URLs and truncates to 500 characters max

set -e

INPUT_FILE="${1:-}"

if [ -z "$INPUT_FILE" ]; then
    echo "Error: No input provided"
    echo "Usage: $0 <input_text_or_file>"
    exit 1
fi

# Check if input is a file or text
if [ -f "$INPUT_FILE" ]; then
    CHANGELOG=$(cat "$INPUT_FILE")
else
    CHANGELOG="$INPUT_FILE"
fi

# Remove markdown URLs with commit hashes
# Pattern: "text ([hash](url))" becomes "text"
# Match any characters in brackets followed by URL
CLEANED=$(echo "$CHANGELOG" | sed -E 's/\s*\(\[[^]]+\]\([^)]+\)\)//g')

# Remove any remaining markdown links [text](url) -> text
CLEANED=$(echo "$CLEANED" | sed -E 's/\[([^]]+)\]\([^)]+\)/\1/g')

# Remove empty bullet points (lines with just * and whitespace)
CLEANED=$(echo "$CLEANED" | grep -v '^\*\s*$' || true)

# Remove excessive blank lines (more than 2 consecutive)
CLEANED=$(echo "$CLEANED" | cat -s)

# Truncate to 500 characters by keeping complete lines
if [ ${#CLEANED} -gt 500 ]; then
    OUTPUT=""
    CURRENT_LENGTH=0
    
    # Read line by line and add until we hit 500 chars
    while IFS= read -r line; do
        LINE_LENGTH=$((${#line} + 1)) # +1 for newline
        NEW_LENGTH=$((CURRENT_LENGTH + LINE_LENGTH))
        
        # Reserve space for "..." at the end (4 chars including newline)
        if [ $NEW_LENGTH -gt 496 ]; then
            break
        fi
        
        if [ -n "$OUTPUT" ]; then
            OUTPUT="$OUTPUT
$line"
        else
            OUTPUT="$line"
        fi
        CURRENT_LENGTH=$NEW_LENGTH
    done <<< "$CLEANED"
    
    CLEANED="$OUTPUT..."
fi

echo "$CLEANED"
