from datetime import datetime

def allowed(options):
    value = options['value'];
    allowed = options['parameter']

    output = {}
    if value in allowed:
        output['success'] = True;
    else:
        output['success'] = False;
        output['message'] = value + ' not in (' + '|'.join(allowed) + ')'

    return output

def dateformat(options):
    formats = options['parameter']
    date = options['value']

    output = {}
    for format in formats:
        try:
            output['success'] = True;
        except ValueError:
            output['success'] = False;
            output['message'] = 'date did not match format (' + '|'.join(formats) + ')'

    return output;