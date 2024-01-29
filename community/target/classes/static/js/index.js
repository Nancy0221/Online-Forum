$(function () {
  $("#publishBtn").click(publish);
});

function publish() {
  $("#publishModal").modal("hide");

  // 获取标题和内容
  var title = $("#recipient-name").val();
  var content = $("#message-text").val();
  // 发送异步请求（POST）
  $.post(
    CONTEXT_PATH + "/discuss/add",
    {
      title: title,
      content: content,
    },
    // 处理服务器返回的结果
    function (data) {
      data = $.parseJSON(data);
      // 在提示框中显示返回的消息
      // 利用text()来改它里面的文本
      $("#hintBody").text(data.msg);
      // 显示提示框
      $("#hintModal").modal("show");
      // 2s后自动隐藏提示框
      setTimeout(function () {
        $("#hintModal").modal("hide");
        // 刷新页面
        if (data.code == 0) {
          // 成功：重新加载页面
          window.location.reload();
        } else {
          alert(data.msg);
        }
      }, 2000);
    }
  );
}
