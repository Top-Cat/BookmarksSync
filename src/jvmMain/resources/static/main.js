let form = document.querySelector("form");
let username = document.getElementById("username");
let dlProgress = document.getElementById("dl-progress");
let upProgress = document.getElementById("up-progress");

let jobDiv = document.getElementById("job")
let jobWaitDiv = document.getElementById("job-wait")
let jobDlDiv = document.getElementById("job-dl")
let jobUpDiv = document.getElementById("job-up")
let jobDoneDiv = document.getElementById("job-done")

if (form) {
    form.onsubmit = (e) => {
        e.preventDefault();
        jobDiv.style.display = "block";
        form.style.display = "none";

        axios.post('/start', username.value)
            .then(response => {
                watchJob(response.data);
            })
            .catch(_ => {
                location.reload();
            });
    };
}

function watchJob(jobId) {
    axios.get(`/state/${jobId}`)
        .then(response => {
            let job = response.data;
            console.log(job);

            if (job.status !== "WAITING") {
                jobWaitDiv.style.display = "none";
            }

            if (job.status === "DOWNLOAD") {
                jobDlDiv.style.display = "block";
                dlProgress.style.width = Math.min(job.progress * 10, 100) + "%";
            }

            if (job.status === "UPLOAD") {
                jobDlDiv.style.display = "block";
                dlProgress.style.width = "100%";

                jobUpDiv.style.display = "block";
                upProgress.style.width = (job.progress * 100) + "%";
            }

            if (job.status === "COMPLETE") {
                jobDlDiv.style.display = "none";
                jobUpDiv.style.display = "none";
                jobDoneDiv.style.display = "block";
            }

            if (job.status !== "ERROR" && job.status !== "COMPLETE") {
                setTimeout(() => watchJob(jobId), 200)
            }
        })
}
