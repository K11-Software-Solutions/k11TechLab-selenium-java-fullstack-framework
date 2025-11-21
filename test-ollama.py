#!/usr/bin/env python3
"""
Quick test to verify Ollama is running and accessible
"""
import requests
import json

def test_ollama_connection():
    try:
        # Test if Ollama is running
        response = requests.get("http://localhost:11434/api/tags")
        if response.status_code == 200:
            models = response.json()
            print("✅ Ollama is running!")
            print(f"Available models: {[model['name'] for model in models['models']]}")
            return True
        else:
            print("❌ Ollama is not responding")
            return False
    except Exception as e:
        print(f"❌ Cannot connect to Ollama: {e}")
        print("Make sure Ollama is installed and running with 'ollama serve'")
        return False

def test_llm_generation():
    try:
        # Test generating a response
        data = {
            "model": "llama3",
            "prompt": "Generate a CSS selector for a login button",
            "stream": False
        }
        response = requests.post("http://localhost:11434/api/generate", json=data)
        if response.status_code == 200:
            result = response.json()
            print("✅ LLM Generation working!")
            print(f"Response: {result['response'][:100]}...")
            return True
        else:
            print("❌ LLM Generation failed")
            return False
    except Exception as e:
        print(f"❌ LLM test failed: {e}")
        return False

if __name__ == "__main__":
    print("🔍 Testing Ollama Connection...")
    if test_ollama_connection():
        print("\n🤖 Testing LLM Generation...")
        test_llm_generation()