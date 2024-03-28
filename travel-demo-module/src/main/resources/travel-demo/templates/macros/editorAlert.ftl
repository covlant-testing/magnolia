[#macro editorAlert message]
    [#if cmsfn.editMode]
    <div class="alert alert-info" role="alert">
        <h4><span class="glyphicon glyphicon-pencil"></span> ${i18n['note.for.editors']}</h4>
        <p>${message}</p>
    </div>
    [/#if]
[/#macro]
