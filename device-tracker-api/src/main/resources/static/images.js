const params = new URLSearchParams(location.search);

const deviceId = params.get("deviceId");

let images = [];

const pageSize = 12;

let currentPage = 1;

console.log("Device ID :", deviceId);
loadFolders();
loadImages();


async function loadImages() {

    const response = await fetch("/media/image/" + deviceId);

    const result = await response.json();

    console.log(result);
    console.log(images[0]);

    images = result.data || [];

    renderPage();

}

function renderPage() {

    const start = (currentPage - 1) * pageSize;

    const end = start + pageSize;

    const page = images.slice(start, end);

    let html = "";

    page.forEach(image => {

        html += `

          <div class="col-6 col-md-4 col-lg-3">
          
              <div class="card image-card">
          
                  <img
                      src="${image.imageUrl}"
                      class="card-img-top"
                      onclick="preview('${image.imageUrl}')">
          
                  <div class="card-body">
          
                      <div class="fw-bold text-truncate">
          
                          ${image.imageName}
          
                      </div>
          
                      <small class="text-muted">
          
                          ${(image.imageSize / 1024 / 1024).toFixed(2)} MB
          
                      </small>
          
                  </div>
          
              </div>
          
          </div>
          
          `;

    });

    document.getElementById("imageGrid").innerHTML = html;

    renderPagination();

}

function renderPagination() {

    let html = "";

    const total =
        Math.ceil(images.length / pageSize);

    for (let i = 1; i <= total; i++) {

        html += `

        <li class="page-item ${i == currentPage ? 'active' : ''}">

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

function preview(url) {

    previewImage.src = url;

    new bootstrap.Modal(

        document.getElementById("previewModal")

    ).show();

}

async function deleteAllImages() {

    if (!confirm("Delete all images?")) {

        return;

    }

    const response =
        await fetch(

            "/media/image/" + deviceId,

            {

                method: "DELETE"

            }

        );

    if (response.ok) {

        alert("Deleted Successfully");

        loadImages();

    }

    else {

        alert("Delete Failed");

    }

}

async function requestImages(bucketId) {

    const payload = {

        deviceId: deviceId,

        bucketId: bucketId,

        limit: parseInt(document.getElementById("imageLimit").value),

        offset: parseInt(document.getElementById("imageOffset").value),

        order: document.getElementById("imageOrder").value

    };

    const response = await fetch("/media/image/refresh", {

        method: "POST",

        headers: {
            "Content-Type": "application/json"
        },

        body: JSON.stringify(payload)

    });

    if (response.ok) {

        alert("Request Sent");

    }

}

async function loadFolders() {

    const response = await fetch("/media/image/folders/" + deviceId);

    const result = await response.json();

    console.log(result);

    const folders = result.data || [];

    let html = "";

    folders.forEach(folder => {

        html += `

        <div class="col-4 col-md-4 col-lg-3">

            <div class="card folder-card h-100">

                <div class="card-body text-center">

                    <div class="folder-icon">
                        📁
                    </div>

                    <div class="folder-name">
                        ${folder.folderName}
                    </div>

                    <div class="text-muted folder-count mb-2">
                        ${folder.imageCount} Images
                    </div>

                    <button
                        class="btn btn-primary btn-sm"
                        onclick="requestImages('${folder.bucketId}')">

                        Get Image

                    </button>

                </div>

            </div>

        </div>

        `;

    });

    document.getElementById("folderGrid").innerHTML = html;

}