// Ajax 요청 함수 정의
function fetchDataFromServer() {
    $.ajax({
        url: "https://www.7-eleven.co.kr/product/presentList.asp",
        async: false,
        success: function (data) {
            location.reload();
        },
        error: function (request, status, error) {
            alert("요청 실패.");
        }
    });
}

document.addEventListener('DOMContentLoaded', function () {
    var products = [];
    var tableBody = document.querySelector('tbody');

    if (products.length > 0) {
        products.forEach(function (product) {
            var row = document.createElement('tr');
            var nameCell = document.createElement('td');
            var descriptionCell = document.createElement('td');
            var priceCell = document.createElement('td');

            nameCell.textContent = product.name;
            descriptionCell.textContent = product.description;
            priceCell.textContent = product.price;

            row.appendChild(nameCell);
            row.appendChild(descriptionCell);
            row.appendChild(priceCell);

            tableBody.appendChild(row);
        });
    }

    fetchDataFromServer();
});
