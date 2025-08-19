System Resource Monitor CLI (Java + OSHI)

A cross-platform terminal monitor for system resources built with Java 17 and OSHI. Tested on Linux Mint.

Features
- CPU usage: per-core and average
- Memory usage: used vs total
- Disk usage: per mounted drive
- Top 5 processes by CPU
- Refreshes every 2 seconds, clears screen in-place
- Press `q` to quit

Requirements
- Java 17+
- Maven 3.8+

Build & Run
```bash
mvn clean install
mvn exec:java -Dexec.mainClass="com.monitor.Main"
```

Example Output
```
System Resource Monitor  2025-08-19 12:21:55
--------------------------------------------------------------------------------
CPU Usage:  11.7% avg
  Core  0:  15.1% [#####                         ]
  Core  1:  13.0% [####                          ]
  Core  2:   8.3% [##                            ]
  Core  3:  10.3% [###                           ]
  Core  4:   9.9% [###                           ]
  Core  5:  18.2% [#####                         ]
  Core  6:   8.8% [###                           ]
  Core  7:  11.8% [####                          ]
--------------------------------------------------------------------------------
Memory: 5.3 GB / 7.4 GB  (71.2%)
--------------------------------------------------------------------------------
Disks:
  Name                   Mount                      Used        Total
  KBG50ZNV512G KIOXIA    /boot/efi                6.1 MB     511.0 MB
--------------------------------------------------------------------------------
Top Processes (by CPU):
  PID     Name                             CPU%        RSS
  9615    cursor                         31.16   439.2 MB
  9572    cursor                         21.22   133.5 MB
  15054   brave                          14.21   232.3 MB
  11348   java                           12.26   957.7 MB
  14472   java                            9.12   147.5 MB
--------------------------------------------------------------------------------
Press 'q' to quit. Refresh: 2s
```

Notes
- Uses ANSI escape sequences for screen clearing and cursor visibility.
- Includes Jansi to improve Windows console handling.
- Designed for extensibility: alerts, logging, and JavaFX GUI can be added later.

License
MIT (or choose your preferred license)


