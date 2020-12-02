function addStaticDialogsHtml(html)
{
    html += '<v-snackbar :timeout="toast_timeout" :bottom=true v-model="toast_model">{{ toast_text }}</v-snackbar>';
    html += '<v-dialog v-model="dialog_model" :persistent="dialog_persistent">' +
    '<v-card>' + 
        '<v-card-title class="headline">{{ dialog_title }}</v-card-title><v-card-text>{{ dialog_content }}</v-card-text>' + 
        '<v-card-actions>' + 
            '<v-spacer></v-spacer>' +
            '<v-btn color="green darken-1" flat="flat" @click.native="dialog_negative_action" v-if="dialog_negative_button_visible">{{ dialog_negative_button }}</v-btn>' +
            '<v-btn color="green darken-1" flat="flat" @click.native="dialog_positive_action" v-if="dialog_positive_button_visible">{{ dialog_positive_button }}</v-btn>' +
        '</v-card-actions>' + 
    '</v-card>' + 
    '</v-dialog>';
    html += '<v-dialog v-model="date_picker_dialog_model" :persistent="date_picker_dialog_persistent">' + 
    '<v-card>' + 
        '<v-card-text>' +
            '<v-date-picker v-model="date_picker_model" :landscape="date_picker_landscape"></v-date-picker>' + 
        '</v-card-text>' +
        '<v-card-actions>' + 
            '<v-spacer></v-spacer>' +
            '<v-btn color="green darken-1" flat="flat" @click.native="date_picker_dialog_negative_action" v-if="date_picker_dialog_negative_button_visible">{{ date_picker_dialog_negative_button }}</v-btn>' +
            '<v-btn color="green darken-1" flat="flat" @click.native="date_picker_dialog_positive_action" v-if="date_picker_dialog_positive_button_visible">{{ date_picker_dialog_positive_button }}</v-btn>' +
        '</v-card-actions>' + 
    '</v-card>' + 
    '</v-dialog>';
    html += '<v-dialog v-model="time_picker_dialog_model" :persistent="time_picker_dialog_persistent">' + 
    '<v-card>' + 
        '<v-card-text>' +
            '<v-time-picker v-model="time_picker_model" :landscape="time_picker_landscape"></v-time-picker>' + 
        '</v-card-text>' +
        '<v-card-actions>' + 
            '<v-spacer></v-spacer>' +
            '<v-btn color="green darken-1" flat="flat" @click.native="time_picker_dialog_negative_action" v-if="time_picker_dialog_negative_button_visible">{{ time_picker_dialog_negative_button }}</v-btn>' +
            '<v-btn color="green darken-1" flat="flat" @click.native="time_picker_dialog_positive_action" v-if="time_picker_dialog_positive_button_visible">{{ time_picker_dialog_positive_button }}</v-btn>' +
        '</v-card-actions>' + 
    '</v-card>' + 
    '</v-dialog>';
    html += '<v-dialog v-model="progress_dialog_model" :persistent="progress_dialog_persistent">' + 
    '<v-card>' + 
        '<v-card-text>' +
            '<v-progress-linear v-model="progress_model" :indeterminate="progress_indeterminate"></v-time-picker>' + 
        '</v-card-text>' +
        '<v-card-actions>' + 
            '<v-spacer></v-spacer>' +
            '<v-btn color="green darken-1" flat="flat" @click.native="progress_dialog_negative_action" v-if="progress_dialog_negative_button_visible">{{ progress_dialog_negative_button }}</v-btn>' +
            '<v-btn color="green darken-1" flat="flat" @click.native="progress_dialog_positive_action" v-if="progress_dialog_positive_button_visible">{{ progress_dialog_positive_button }}</v-btn>' +
        '</v-card-actions>' + 
    '</v-card>' + 
    '</v-dialog>'
    return html;
}

function addStaticDialogsModel(model)
{
    model['toast_timeout'] = 5000;
    model['toast_model'] = false;
    model['toast_text'] = "test";

    model['dialog_model'] = false;
    model['dialog_persistent'] = false;
    model['dialog_title'] = "";
    model['dialog_content'] = "";
    model['dialog_positive_button'] = "";
    model['dialog_positive_button_visible'] = false;
    model['dialog_negative_button'] = "";
    model['dialog_negative_button_visible'] = false;

    model['date_picker_dialog_model'] = false;
    model['date_picker_dialog_persistent'] = false;
    model['date_picker_model'] = null;
    model['date_picker_landscape'] = false;
    model['date_picker_dialog_positive_button'] = "";
    model['date_picker_dialog_positive_button_visible'] = false;
    model['date_picker_dialog_negative_button'] = "";
    model['date_picker_dialog_negative_button_visible'] = false;

    model['time_picker_dialog_model'] = false;
    model['time_picker_dialog_persistent'] = false;
    model['time_picker_model'] = null;
    model['time_picker_landscape'] = false;
    model['time_picker_dialog_positive_button'] = "";
    model['time_picker_dialog_positive_button_visible'] = false;
    model['time_picker_dialog_negative_button'] = "";
    model['time_picker_dialog_negative_button_visible'] = false;

    model['progress_dialog_model'] = false;
    model['progress_dialog_persistent'] = false;
    model['progress_model'] = 0;
    model['progress_indeterminate'] = false;
    model['progress_dialog_positive_button'] = "";
    model['progress_dialog_positive_button_visible'] = false;
    model['progress_dialog_negative_button'] = "";
    model['progress_dialog_negative_button_visible'] = false;

    return model;
}

function addStaticDialogsWatch(watch)
{
    watch.watch.push(
        {
            id: "date_picker_dialog_model",
            idWatch: "date_picker_dialog_model"
        },
        {
            id: "date_picker_model",
            idWatch: "date_picker_model"
        },
        {
            id: "time_picker_dialog_model",
            idWatch: "time_picker_dialog_model"
        },
        {
            id: "time_picker_model",
            idWatch: "time_picker_model"
        });
    return watch;
}