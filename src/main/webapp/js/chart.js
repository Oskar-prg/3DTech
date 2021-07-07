$(document).ready(function () {
    const path = document.querySelector("#ctxPath").value
    $.post(path + "/controlpanel/chart",
        function (data) {
            let ctx = document.getElementById('chart').getContext('2d')
            let chart = new Chart(ctx, {
                type: 'bar',
                data: {
                    labels: data[0],
                    datasets: [{
                        label: '# scorte',
                        data: data[1],
                        //todo: eliminare proprietà options
                        options: {
                            responsive: true,
                        },
                        backgroundColor: [
                            'rgba(255, 99, 132, 0.2)',
                            'rgba(54, 162, 235, 0.2)',
                            'rgba(255, 206, 86, 0.2)',
                            'rgba(75, 192, 192, 0.2)',
                            'rgba(153, 102, 255, 0.2)',
                            'rgba(255, 159, 64, 0.2)'
                        ],
                        borderColor: [
                            'rgba(255, 99, 132, 1)',
                            'rgba(54, 162, 235, 1)',
                            'rgba(255, 206, 86, 1)',
                            'rgba(75, 192, 192, 1)',
                            'rgba(153, 102, 255, 1)',
                            'rgba(255, 159, 64, 1)'
                        ],
                        borderWidth: 1
                    }]

                }
            })
        }
    )
})