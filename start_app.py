#!/usr/bin/env python3
"""
Edgefit-Coach Application Launcher
Starts all components in the correct order with proper delays.
"""

import os
import sys
import subprocess
import time
import signal
import webbrowser
import threading

# Set encoding for Windows compatibility
os.environ['PYTHONIOENCODING'] = 'utf-8'

def start_component(name, command, delay=0, show_output=False):
    """Start a component with error handling."""
    print(f"🚀 Starting {name}...")
    try:
        if delay > 0:
            print(f"⏳ Waiting {delay} seconds before starting {name}...")
            time.sleep(delay)
        
        if show_output:
            # For API server, show output in real-time for debugging
            process = subprocess.Popen(
                command,
                text=True,
                encoding='utf-8',
                errors='ignore'
            )
        else:
            # For other components, capture output to prevent clutter
            process = subprocess.Popen(
                command,
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                text=True,
                encoding='utf-8',
                errors='ignore'
            )
        
        print(f"✅ {name} started successfully (PID: {process.pid})")
        return process
    except Exception as e:
        print(f"❌ Failed to start {name}: {e}")
        return None


def main():
    print("🚀 Edgefit-Coach Application Launcher")
    print("=" * 50)
    
    processes = []
    
    try:
        # Step 1: Start WebSocket Server
        websocket_process = start_component(
            "WebSocket Server",
            ["python", "websocket_server.py"]
        )
        if websocket_process:
            processes.append(("WebSocket Server", websocket_process))
        
        # Step 2: Start API Server (with delay and verbose output)
        api_process = start_component(
            "API Server",
            ["python", "api_server.py"],
            delay=3,
            show_output=True  # Show API server logs for debugging
        )
        if api_process:
            processes.append(("API Server", api_process))
        
        print("=" * 50)
        print("🎉 ALL COMPONENTS STARTED!")
        print("📋 Access your application APIs at:")
        print("   🔧 API Docs: http://localhost:8000/docs")
        print("   🔌 WebSocket: ws://localhost:8001")
        print("=" * 50)

        
        print("Press Ctrl+C to stop all components...")
        
        # Wait for user interrupt
        while True:
            time.sleep(1)
            # Check if any process has died
            for name, process in processes:
                if process.poll() is not None:
                    print(f"⚠️ {name} has stopped unexpectedly")
    
    except KeyboardInterrupt:
        print("\n🛑 Shutting down all components...")
        
        # Stop all processes
        for name, process in processes:
            try:
                print(f"🛑 Stopping {name}...")
                process.terminate()
                process.wait(timeout=5)
                print(f"✅ {name} stopped")
            except subprocess.TimeoutExpired:
                print(f"🔪 Force killing {name}...")
                process.kill()
            except Exception as e:
                print(f"❌ Error stopping {name}: {e}")
        
        print("✅ All components stopped")
    
    except Exception as e:
        print(f"❌ Error: {e}")
        # Clean up processes
        for name, process in processes:
            try:
                process.terminate()
            except:
                pass

if __name__ == "__main__":
    main() 