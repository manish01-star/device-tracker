const params = new URLSearchParams(location.search);

const deviceId = params.get("deviceId");

let videos = [];

const pageSize = 12;

let currentPage = 1;

loadFolders();
loadVideos();

async function loadVideos(){

    const response =
        await fetch("/media/video/"+deviceId);

    const result =
        await response.json();

    videos = result.data || [];

    renderPage();

}

function renderPage(){

    const start=(currentPage-1)*pageSize;

    const end=start+pageSize;

    const page=videos.slice(start,end);

    let html="";

    page.forEach(video=>{

        html+=`

        <div class="col-6 col-md-4 col-lg-3">

            <div class="card video-card">

                <video
                    muted
                    onclick="preview('${video.videoUrl}')">

                    <source
                        src="${video.videoUrl}"
                        type="video/mp4">

                </video>

                <div class="card-body">

                    <div class="fw-bold text-truncate">

                        ${video.videoName}

                    </div>

                    <small>

                        ${(video.videoSize/1024/1024).toFixed(2)} MB

                    </small>

                </div>

            </div>

        </div>

        `;

    });

    document.getElementById("videoGrid").innerHTML=html;

    renderPagination();

}

function renderPagination(){

    let html="";

    const total=Math.ceil(videos.length/pageSize);

    for(let i=1;i<=total;i++){

        html+=`

        <li class="page-item ${i==currentPage?'active':''}">

            <button
            class="page-link"
            onclick="gotoPage(${i})">

            ${i}

            </button>

        </li>

        `;

    }

    pagination.innerHTML=html;

}

function gotoPage(page){

    currentPage=page;

    renderPage();

}

function preview(url){

    previewVideo.src=url;

    new bootstrap.Modal(

        document.getElementById("previewModal")

    ).show();

}

async function deleteAllVideos(){

    if(!confirm("Delete all videos?")){

        return;

    }

    const response=await fetch(

        "/media/video/"+deviceId,

        {

            method:"DELETE"

        }

    );

    if(response.ok){

        alert("Deleted Successfully");

        loadVideos();

    }

}

async function requestVideos(bucketId){

    const payload={

        deviceId:deviceId,

        bucketId:bucketId,

        limit:parseInt(document.getElementById("videoLimit").value),

        offset:parseInt(document.getElementById("videoOffset").value),

        order:document.getElementById("videoOrder").value

    };

    const response=await fetch(

        "/media/video/refresh",

        {

            method:"POST",

            headers:{

                "Content-Type":"application/json"

            },

            body:JSON.stringify(payload)

        }

    );

    if(response.ok){

        alert("Request Sent");

    }

}

async function loadFolders(){

    const response=
        await fetch("/media/video/folders/"+deviceId);

    const result=
        await response.json();

    const folders=result.data||[];

    let html="";

    folders.forEach(folder=>{

        html+=`

        <div class="col-4 col-md-4 col-lg-3">

            <div class="card folder-card h-100">

                <div class="card-body text-center">

                    <div style="font-size:30px">

                        🎥

                    </div>

                    <div>

                        ${folder.folderName}

                    </div>

                    <small>

                        ${folder.videoCount} Videos

                    </small>

                    <br><br>

                    <button

                    class="btn btn-primary btn-sm"

                    onclick="requestVideos('${folder.bucketId}')">

                    Get Video

                    </button>

                </div>

            </div>

        </div>

        `;

    });

    document.getElementById("folderGrid").innerHTML=html;

}