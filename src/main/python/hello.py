from datetime import datetime

def hello(data):
    print data

def allowed(options):
    value = options['value'];
    allowed = options['parameter']

    if value in allowed:
        return True;
    else:
        return False;

def dateformat(options):
    formats = options['parameter']
    date = options['value']

    for format in formats:
        try:
            return True;
        except ValueError:
            pass

    return False;

def mindate(options):
    date = datetime.strptime(options['value'], '%Y-%m-%d')
    min = datetime.strptime(options['parameter'], '%Y-%m-%d')

    if (date > min):
        return True;
    else:
        return False;

def maxdate(options):
    date = datetime.strptime(options['value'], '%Y-%m-%d')
    max = datetime.strptime(options['parameter'], '%Y-%m-%d')

    if (date < max):
        return True;
    else:
        return False;